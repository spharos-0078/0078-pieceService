package com.pieceofcake.piece_service.trade.application;

import com.pieceofcake.piece_service.common.entity.BaseResponseStatus;
import com.pieceofcake.piece_service.common.exception.BaseException;
import com.pieceofcake.piece_service.piece.infrastructure.PieceProductRepository;
import com.pieceofcake.piece_service.piece.infrastructure.PieceRepository;
import com.pieceofcake.piece_service.trade.dto.in.CreateTradeRequestDto;
import com.pieceofcake.piece_service.trade.dto.out.GetAllPieceProductUuidResponseDto;
import com.pieceofcake.piece_service.trade.dto.out.GetOwnedMemberAndPieceQuantityResponseDto;
import com.pieceofcake.piece_service.trade.dto.out.GetOwnedPieceResponseDto;
import com.pieceofcake.piece_service.trade.entity.OwnedPiece;
import com.pieceofcake.piece_service.trade.entity.PieceTradeReservation;
import com.pieceofcake.piece_service.trade.entity.TradeStatus;
import com.pieceofcake.piece_service.trade.infrastructure.OwnedPieceRepository;
import com.pieceofcake.piece_service.trade.infrastructure.TradeReservationRepository;
import com.pieceofcake.piece_service.trade.infrastructure.feign.client.PaymentFeignClient;
import com.pieceofcake.piece_service.trade.infrastructure.feign.dto.BaseResponse;
import com.pieceofcake.piece_service.trade.infrastructure.feign.dto.ReadMoneyAmountResponseDto;
import com.pieceofcake.piece_service.trade.infrastructure.redis.RedisPublisher;
import com.pieceofcake.piece_service.trade.scheduler.TradingTimeChecker;
import com.pieceofcake.piece_service.trade.util.PriceStepValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class TradeServiceImpl implements TradeService {

    private final OwnedPieceRepository ownedPieceRepository;
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
    public List<GetAllPieceProductUuidResponseDto> getOwnedPieceProductUuidByMemberUuid(String memberUuid) {
        List<OwnedPiece> pieces = ownedPieceRepository.findByMemberUuid(memberUuid);

        return pieces.stream().map(GetAllPieceProductUuidResponseDto::from).toList();
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
        // 거래 가능 시간 검증
        if (!tradingTimeChecker.isMarketOpen()) {
            throw new IllegalStateException("현재는 거래 가능한 시간이 아닙니다.");
        }

        String pieceProductUuid = createTradeRequestDto.getPieceProductUuid();
        long price = createTradeRequestDto.getRegisteredPrice();

        // 현재가 조회
        String lastPriceStr = redisTemplate
                .opsForValue()
                .get("pieceLastPrice:" + pieceProductUuid);
        if (lastPriceStr == null) {
            lastPriceStr = pieceProductRepository.findByPieceProductUuid(pieceProductUuid)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 조각상품입니다."))
                    .getMarketPrice().toString();
        }
        long currentPrice = Long.parseLong(lastPriceStr);

        // 호가 단위 검증
        if (!PriceStepValidator.isValidPrice(price, currentPrice)) {
            long step = PriceStepValidator.getPriceStep(price);
            throw new IllegalArgumentException("해당 가격대의 호가 단위는 " + step + "원이며, 현재가는 " + currentPrice + "원입니다.");
        }

        long totalPrice = createTradeRequestDto.getRegisteredPrice() * createTradeRequestDto.getDesiredQuantity();
        long currentAmount = getAvailableAmount(memberUuid);

        if(currentAmount < totalPrice) {
            throw new IllegalArgumentException("예치금이 부족합니다.");
        }

        // 2. 예약 등록
        PieceTradeReservation buyReservation = createTradeRequestDto.toBuyEntity(memberUuid);
        tradeReservationRepository.save(buyReservation);

        // Redis에 예약 전송
        redisPublisher.publishOrderBook(buyReservation.getPieceProductUuid(), buyReservation.getRegisteredPrice(),
                buyReservation.getDesiredQuantity(), buyReservation.getTradeType().name());

        // 최신 호가 요약 생성
        Map<String, Object> summary = orderbookSummaryService.buildOrderbookSummary(buyReservation.getPieceProductUuid());

        // ✅ 예약 이벤트 발행 (공통 메서드 사용)
//        redisPublisher.publishTradeReservedEvent(
//                "BUY", memberUuid,
//                buyReservation.getPieceProductUuid(),
//                buyReservation.getRegisteredPrice(),
//                buyReservation.getDesiredQuantity(),
//                buyReservation.getCreatedAt()
//        );
        redisPublisher.publishOrderbookSummary(buyReservation.getPieceProductUuid(), summary);

        // 3. 체결 시도
        matchingService.match(buyReservation);

    }

    /** 매도 예약 등록 */
    @Transactional
    @Override
    public void createSellReservation(String memberUuid, CreateTradeRequestDto createTradeRequestDto) {
        // 거래 가능 시간 검증
        if (!tradingTimeChecker.isMarketOpen()) {
            throw new IllegalStateException("현재는 거래 가능한 시간이 아닙니다.");
        }

        String pieceProductUuid = createTradeRequestDto.getPieceProductUuid();
        long price = createTradeRequestDto.getRegisteredPrice();

        // 현재가 조회
        String lastPriceStr = redisTemplate
                .opsForValue()
                .get("pieceLastPrice:" + pieceProductUuid);
        if (lastPriceStr == null) {
            lastPriceStr = pieceProductRepository.findByPieceProductUuid(pieceProductUuid)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 조각상품입니다."))
                    .getMarketPrice().toString();
        }
        long currentPrice = Long.parseLong(lastPriceStr);

        // 호가 단위 검증
        if (!PriceStepValidator.isValidPrice(price, currentPrice)) {
            long step = PriceStepValidator.getPriceStep(price);
            throw new IllegalArgumentException("해당 가격대의 호가 단위는 " + step + "원이며, 현재가는 " + currentPrice + "원입니다.");
        }

        // 1. 보유 조각 검증
        List<OwnedPiece> ownedPieces = ownedPieceRepository
                .findByMemberUuidAndPieceProductUuid(memberUuid, createTradeRequestDto.getPieceProductUuid());

        if(ownedPieces.size() < createTradeRequestDto.getDesiredQuantity()) {
            throw new IllegalArgumentException("보유 조각이 부족합니다.");
        }

        // 2. 조각 유효성 검증
        for(OwnedPiece piece: ownedPieces) {
            if(!pieceRepository.existsByPieceUuid(piece.getPieceUuid())) {
                throw new IllegalArgumentException("존재하지 않는 조각입니다.");
            }
        }

        // 3. 예약 생성 (dto에 위임)
        PieceTradeReservation sellReservation = createTradeRequestDto.toSellEntity(memberUuid);
        tradeReservationRepository.save(sellReservation);

        // Redis에 예약 전송
        redisPublisher.publishOrderBook(sellReservation.getPieceProductUuid(), sellReservation.getRegisteredPrice(),
                sellReservation.getDesiredQuantity(), sellReservation.getTradeType().name());

        // 최신 호가 요약 생성
        Map<String, Object> summary = orderbookSummaryService.buildOrderbookSummary(sellReservation.getPieceProductUuid());

        redisPublisher.publishOrderbookSummary(sellReservation.getPieceProductUuid(), summary);

//        // ✅ 예약 이벤트 발행 (공통 메서드 사용)
//        redisPublisher.publishTradeReservedEvent(
//                "SELL", memberUuid,
//                sellReservation.getPieceProductUuid(),
//                sellReservation.getRegisteredPrice(),
//                sellReservation.getDesiredQuantity(),
//                sellReservation.getCreatedAt()
//        );

        // 4. 체결 시도
        matchingService.match(sellReservation);
    }

    private long getAvailableAmount(String memberUuid) {
        BaseResponse<ReadMoneyAmountResponseDto> amount = paymentFeignClient.getMoney(memberUuid);
        return amount.getResult().getAmount();
    }

    @Transactional
    @Override
    public void cancelReservation(String memberUuid, String reservationUuid) {
        PieceTradeReservation reservation = tradeReservationRepository.findByReservationUuid(reservationUuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_RESERVATION));

        // 본인 예약이 아닌 경우 예외
        if (!reservation.getMemberUuid().equals(memberUuid)) {
            throw new BaseException(BaseResponseStatus.NO_AUTH_RESERVATION);
        }

        // 이미 완료된 거래는 취소 불가
        if (reservation.getTradeStatus() == TradeStatus.COMPLETED) {
            throw new BaseException(BaseResponseStatus.ALREADY_COMPLETED_RESERVATION);
        }

        reservation.cancel();
    }
}
