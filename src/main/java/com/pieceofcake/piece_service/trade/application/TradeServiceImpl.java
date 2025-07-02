package com.pieceofcake.piece_service.trade.application;

import com.pieceofcake.piece_service.common.entity.BaseResponseStatus;
import com.pieceofcake.piece_service.common.exception.BaseException;
import com.pieceofcake.piece_service.piece.infrastructure.PieceProductRepository;
import com.pieceofcake.piece_service.piece.infrastructure.PieceRepository;
import com.pieceofcake.piece_service.trade.dto.in.CreateTradeRequestDto;
import com.pieceofcake.piece_service.trade.dto.out.*;
import com.pieceofcake.piece_service.trade.entity.OwnedPiece;
import com.pieceofcake.piece_service.trade.entity.OwnedPieceAverage;
import com.pieceofcake.piece_service.trade.entity.PieceTradeReservation;
import com.pieceofcake.piece_service.trade.infrastructure.OwnedPieceAverageRepository;
import com.pieceofcake.piece_service.trade.infrastructure.OwnedPieceRepository;
import com.pieceofcake.piece_service.trade.infrastructure.TradeReservationRepository;
import com.pieceofcake.piece_service.trade.infrastructure.feign.client.PaymentFeignClient;
import com.pieceofcake.piece_service.trade.infrastructure.feign.dto.BaseResponse;
import com.pieceofcake.piece_service.trade.infrastructure.feign.dto.ReadMoneyAmountResponseDto;
import com.pieceofcake.piece_service.trade.infrastructure.redis.RedisPublisher;
import com.pieceofcake.piece_service.trade.scheduler.TradingTimeChecker;
import com.pieceofcake.piece_service.trade.util.PriceStepValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class TradeServiceImpl implements TradeService {

    private final OwnedPieceRepository ownedPieceRepository;
    private final OwnedPieceAverageRepository ownedPieceAverageRepository;
    private final TradeReservationRepository tradeReservationRepository;
    private final PaymentFeignClient paymentFeignClient;
    private final PieceRepository pieceRepository;
    private final MatchingService matchingService;
    private final RedisPublisher redisPublisher;
    private final RedisTemplate<String, String> redisTemplate;
    private final OrderbookSummaryService orderbookSummaryService;
    private final TradingTimeChecker tradingTimeChecker;
    private final PieceProductRepository pieceProductRepository;

    @Override
    public List<GetOwnedMemberAndPieceQuantityResponseDto> getOwnedMemberAndQuantity(String pieceProductUuid) {
        List<Object[]> result = ownedPieceRepository.countOwnedMemberUuidAndQuantityByPieceProductUuid(pieceProductUuid);

        return result.stream().map(o -> {
            String memberUuid = (String) o[0];
            Long count = (Long) o[1];
            return new GetOwnedMemberAndPieceQuantityResponseDto(memberUuid, count.intValue());
        }).toList();
    }

    @Override
    public List<GetOwnedPieceResponseDto> getOwnedPieceByMemberUuid(String memberUuid) {
        List<Object[]> result = ownedPieceRepository.countOwnedPiecesByMemberUuid(memberUuid);

        return result.stream().map(o -> {
            String pieceProductUuid = (String) o[0];
            Long count = (Long) o[1];
            return new GetOwnedPieceResponseDto(pieceProductUuid, count.intValue());
        }).toList();
    }

    @Override
    public GetOwnedPieceResponseDto getOwnedPieceByMemberAndProductUuid(String memberUuid, String pieceProductUuid) {
        Long count = ownedPieceRepository.countByMemberUuidAndPieceProductUuid(memberUuid, pieceProductUuid);
        return new GetOwnedPieceResponseDto(pieceProductUuid, count.intValue());
    }

    /** 매수 예약 등록 **/
    @Transactional
    @Override
    public void createBuyReservation(String memberUuid, CreateTradeRequestDto createTradeRequestDto) {
        // 1. 거래 가능 시간 검증
        validateTradingTime();
        
        // 2. 가격 검증
        validatePrice(createTradeRequestDto);
        
        // 3. 예치금 검증
        validateBalance(memberUuid, createTradeRequestDto);
        
        // 4. 예약 등록
        PieceTradeReservation buyReservation = createTradeRequestDto.toBuyEntity(memberUuid);
        tradeReservationRepository.save(buyReservation);
        
        // 5. Redis 이벤트 발행
        publishOrderBookEvents(buyReservation);
        
        // 6. 매칭 시도
        matchingService.match(buyReservation);
    }

    /** 매도 예약 등록 */
    @Transactional
    @Override
    public void createSellReservation(String memberUuid, CreateTradeRequestDto createTradeRequestDto) {
        // 1. 거래 가능 시간 검증
        validateTradingTime();
        
        // 2. 가격 검증
        validatePrice(createTradeRequestDto);
        
        // 3. 보유 조각 검증
        validateOwnedPieces(memberUuid, createTradeRequestDto);
        
        // 4. 예약 등록
        PieceTradeReservation sellReservation = createTradeRequestDto.toSellEntity(memberUuid);
        tradeReservationRepository.save(sellReservation);
        
        // 5. Redis 이벤트 발행
        publishOrderBookEvents(sellReservation);
        
        // 6. 매칭 시도
        matchingService.match(sellReservation);
    }

    @Override
    public List<GetAllPieceProductUuidResponseDto> getOwnedPieceProductUuidByMemberUuid(String memberUuid) {
        List<OwnedPieceAverage> pieces = ownedPieceAverageRepository.findByMemberUuid(memberUuid);
        return pieces.stream().map(GetAllPieceProductUuidResponseDto::from).toList();
    }

    @Override
    public GetPieceAverageResponseDto getPieceAverageByMemberAndPieceProductUuid(String memberUuid, String pieceProductUuid) {
        OwnedPieceAverage ownedPieceAverage = ownedPieceAverageRepository
                .findByMemberUuidAndPieceProductUuid(memberUuid, pieceProductUuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_OWNED_PIECE_AVERAGE));

        return GetPieceAverageResponseDto.from(ownedPieceAverage);
    }

    @Transactional
    @Override
    public void cancelReservation(String memberUuid, String reservationUuid) {
        PieceTradeReservation reservation = tradeReservationRepository
                .findByReservationUuid(reservationUuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_RESERVATION_FOUND));

        if (!reservation.getMemberUuid().equals(memberUuid)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACCESS);
        }

        reservation.cancel();
        tradeReservationRepository.save(reservation);
        
        log.info("예약 취소 완료: reservationUuid={}, memberUuid={}", reservationUuid, memberUuid);
    }

    @Override
    public List<GetTradeReservationUuidResponseDto> getReservationUuidByMemberUuid(String memberUuid) {
        List<PieceTradeReservation> reservations = tradeReservationRepository.findByMemberUuid(memberUuid);
        return reservations.stream()
                .map(GetTradeReservationUuidResponseDto::from)
                .toList();
    }

    @Override
    public GetTradeReservationResponseDto getReservationByUuid(String memberUuid, String reservationUuid) {
        PieceTradeReservation reservation = tradeReservationRepository
                .findByReservationUuid(reservationUuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_RESERVATION_FOUND));

        if (!reservation.getMemberUuid().equals(memberUuid)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACCESS);
        }

        return GetTradeReservationResponseDto.from(reservation);
    }

    // Private validation methods
    private void validateTradingTime() {
        if (!tradingTimeChecker.isMarketOpen()) {
            throw new BaseException(BaseResponseStatus.TRADING_TIME_NOT_AVAILABLE);
        }
    }

    private void validatePrice(CreateTradeRequestDto createTradeRequestDto) {
        String pieceProductUuid = createTradeRequestDto.getPieceProductUuid();
        long price = createTradeRequestDto.getRegisteredPrice();

        // 현재가 조회
        String lastPriceStr = redisTemplate.opsForValue().get("pieceLastPrice:" + pieceProductUuid);
        if (lastPriceStr == null) {
            lastPriceStr = pieceProductRepository.findByPieceProductUuid(pieceProductUuid)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_PIECE_PRODUCT_FOUND))
                    .getMarketPrice().toString();
        }
        long currentPrice = Long.parseLong(lastPriceStr);

        // 호가 단위 검증
        if (!PriceStepValidator.isValidPrice(price, currentPrice)) {
            long step = PriceStepValidator.getPriceStep(price);
            throw new BaseException(BaseResponseStatus.INVALID_PRICE_STEP);
        }
    }

    private void validateBalance(String memberUuid, CreateTradeRequestDto createTradeRequestDto) {
        long totalPrice = createTradeRequestDto.getRegisteredPrice() * createTradeRequestDto.getDesiredQuantity();
        long currentAmount = getAvailableAmount(memberUuid);

        if (currentAmount < totalPrice) {
            throw new BaseException(BaseResponseStatus.INSUFFICIENT_BALANCE);
        }
    }

    private void validateOwnedPieces(String memberUuid, CreateTradeRequestDto createTradeRequestDto) {
        List<OwnedPiece> ownedPieces = ownedPieceRepository
                .findByMemberUuidAndPieceProductUuid(memberUuid, createTradeRequestDto.getPieceProductUuid());

        if (ownedPieces.size() < createTradeRequestDto.getDesiredQuantity()) {
            throw new BaseException(BaseResponseStatus.INSUFFICIENT_PIECES);
        }

        // 조각 유효성 검증
        for (OwnedPiece piece : ownedPieces) {
            if (!pieceRepository.existsByPieceUuid(piece.getPieceUuid())) {
                throw new BaseException(BaseResponseStatus.INVALID_PIECE);
            }
        }
    }

    private void publishOrderBookEvents(PieceTradeReservation reservation) {
        // Redis에 예약 전송
        redisPublisher.publishOrderBook(reservation.getPieceProductUuid(), reservation.getRegisteredPrice(),
                reservation.getDesiredQuantity(), reservation.getTradeType().name());

        // 최신 호가 요약 생성
        Map<String, Object> summary = orderbookSummaryService.buildOrderbookSummary(reservation.getPieceProductUuid());
        redisPublisher.publishOrderbookSummary(reservation.getPieceProductUuid(), summary);
    }

    private long getAvailableAmount(String memberUuid) {
        try {
            BaseResponse<ReadMoneyAmountResponseDto> response = paymentFeignClient.getMoney(memberUuid);
            return response.getResult().getAmount();
        } catch (Exception e) {
            log.error("예치금 조회 실패: memberUuid={}", memberUuid, e);
            throw new BaseException(BaseResponseStatus.PAYMENT_SERVICE_ERROR);
        }
    }
}
