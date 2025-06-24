package com.pieceofcake.piece_service.trade.application;

import com.pieceofcake.piece_service.piece.infrastructure.PieceRepository;
import com.pieceofcake.piece_service.trade.dto.in.CreateTradeRequestDto;
import com.pieceofcake.piece_service.trade.dto.out.GetAllPieceProductUuidResponseDto;
import com.pieceofcake.piece_service.trade.dto.out.GetOwnedMemberAndPieceQuantityResponseDto;
import com.pieceofcake.piece_service.trade.dto.out.GetOwnedPieceResponseDto;
import com.pieceofcake.piece_service.trade.entity.OwnedPiece;
import com.pieceofcake.piece_service.trade.entity.PieceTradeReservation;
import com.pieceofcake.piece_service.trade.infrastructure.OwnedPieceRepository;
import com.pieceofcake.piece_service.trade.infrastructure.TradeReservationRepository;
import com.pieceofcake.piece_service.trade.infrastructure.TradedHistoryRepository;
import com.pieceofcake.piece_service.trade.infrastructure.feign.client.PaymentFeignClient;
import com.pieceofcake.piece_service.trade.infrastructure.feign.dto.BaseResponse;
import com.pieceofcake.piece_service.trade.infrastructure.feign.dto.ReadMoneyAmountResponseDto;
import com.pieceofcake.piece_service.trade.infrastructure.redis.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TradeServiceImpl implements TradeService {

    private final OwnedPieceRepository ownedPieceRepository;
    private final TradedHistoryRepository tradedHistoryRepository;
    private final TradeReservationRepository tradeReservationRepository;
    private final PaymentFeignClient paymentFeignClient;
    private final PieceRepository pieceRepository;
    private final MatchingService matchingService;
    private final RedisPublisher redisPublisher;

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
        redisPublisher.validateMarketOpen();

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


        // 3. 체결 시도
        matchingService.match(buyReservation);

    }

    /** 매도 예약 등록 */
    @Transactional
    @Override
    public void createSellReservation(String memberUuid, CreateTradeRequestDto createTradeRequestDto) {
        // 거래 가능 시간 검증
        redisPublisher.validateMarketOpen();

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

        // 4. 체결 시도
        matchingService.match(sellReservation);
    }

    private long getAvailableAmount(String memberUuid) {
        BaseResponse<ReadMoneyAmountResponseDto> amount = paymentFeignClient.getMoney(memberUuid);
        return amount.getResult().getAmount();
    }

}
