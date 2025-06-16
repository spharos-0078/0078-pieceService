package com.pieceofcake.piece_service.trade.application;

import com.pieceofcake.piece_service.piece.infrastructure.PieceRepository;
import com.pieceofcake.piece_service.trade.dto.in.CreateTradedHistoryRequestDto;
import com.pieceofcake.piece_service.trade.dto.in.TransferPieceOwnershipRequestDto;
import com.pieceofcake.piece_service.trade.entity.OwnedPiece;
import com.pieceofcake.piece_service.trade.entity.PieceTradeReservation;
import com.pieceofcake.piece_service.trade.entity.TradeType;
import com.pieceofcake.piece_service.trade.infrastructure.OwnedPieceRepository;
import com.pieceofcake.piece_service.trade.infrastructure.TradeReservationRepository;
import com.pieceofcake.piece_service.trade.infrastructure.TradedHistoryRepository;
import com.pieceofcake.piece_service.trade.infrastructure.feign.client.PaymentFeignClient;
import com.pieceofcake.piece_service.trade.infrastructure.feign.dto.CreateMoneyRequestFeignDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MatchingServiceImpl implements MatchingService {

    private final OwnedPieceRepository ownedPieceRepository;
    private final TradedHistoryRepository tradedHistoryRepository;
    private final TradeReservationRepository tradeReservationRepository;
    private final PaymentFeignClient paymentFeignClient;
    private final PieceRepository pieceRepository;

    @Transactional
    @Override
    public void match(PieceTradeReservation reservation) {
        if (reservation.getTradeType() == TradeType.BUY) {
            matchBuy(reservation);
        } else {
            matchSell(reservation);
        }
    }

    /** BUY 매칭 **/
    private void matchBuy(PieceTradeReservation buyReservation) {
        List<PieceTradeReservation> sellReservations = tradeReservationRepository
                .findMatchingSellReservations(buyReservation.getPieceProductUuid(), buyReservation.getRegisteredPrice());

        for (PieceTradeReservation sell : sellReservations) {
            if (isSelfTrade(buyReservation, sell)) continue;

            int matchQuantity = calculateMatchQuantity(buyReservation, sell);
            executeTrade(buyReservation, sell, matchQuantity);

            if (buyReservation.isFullMatched()) break;
        }
        tradeReservationRepository.save(buyReservation);
    }

    /** SELL 매칭 **/
    private void matchSell(PieceTradeReservation sellReservation) {
        List<PieceTradeReservation> buyReservations = tradeReservationRepository
                .findMatchingBuyReservations(sellReservation.getPieceProductUuid(), sellReservation.getRegisteredPrice());

        for (PieceTradeReservation buy : buyReservations) {
            if (isSelfTrade(sellReservation, buy)) continue;

            int matchQuantity = calculateMatchQuantity(sellReservation, buy);
            executeTrade(buy, sellReservation, matchQuantity);

            if (sellReservation.isFullMatched()) break;
        }
        tradeReservationRepository.save(sellReservation);
    }

    /** 핵심 체결 로직 (공통) **/
    private void executeTrade(PieceTradeReservation buy, PieceTradeReservation sell, int matchQuantity) {
        List<OwnedPiece> sellerPieces = ownedPieceRepository
                .findByMemberUuidAndPieceProductUuid(sell.getMemberUuid(), sell.getPieceProductUuid())
                .subList(0, matchQuantity);

        for (OwnedPiece piece : sellerPieces) {
            transferOwnership(piece, buy.getMemberUuid());
            saveTradeHistory(buy, sell, piece);
        }

        // 예치금 차감 및 정산
        long totalPrice = matchQuantity * sell.getRegisteredPrice();

        // BUY -> 예치금 차감
        paymentFeignClient.createMoney(buy.getMemberUuid(), CreateMoneyRequestFeignDto.buy(totalPrice));

        // SELL -> 예치금 입금
        paymentFeignClient.createMoney(sell.getMemberUuid(), CreateMoneyRequestFeignDto.sell(totalPrice));

        buy.reduceQuantity(matchQuantity);
        sell.reduceQuantity(matchQuantity);
        tradeReservationRepository.save(sell);
        tradeReservationRepository.save(buy);
    }

    /** 소유권 이전 **/
    private void transferOwnership(OwnedPiece piece, String newOwnerUuid) {
        OwnedPiece newOwnedPiece = TransferPieceOwnershipRequestDto.of(piece, newOwnerUuid).toEntity();
        ownedPieceRepository.save(newOwnedPiece);
        ownedPieceRepository.delete(piece);
        pieceRepository.updateOwner(piece.getPieceUuid(), newOwnerUuid);
    }

    /** 거래내역 기록 **/
    private void saveTradeHistory(PieceTradeReservation buy, PieceTradeReservation sell, OwnedPiece piece) {
        tradedHistoryRepository.save(CreateTradedHistoryRequestDto.of(
                buy, piece.getPieceUuid(), TradeType.BUY, buy.getMemberUuid()).toEntity());

        tradedHistoryRepository.save(CreateTradedHistoryRequestDto.of(
                sell, piece.getPieceUuid(), TradeType.SELL, sell.getMemberUuid()).toEntity());
    }

    /** 매칭 수량 계산 **/
    private int calculateMatchQuantity(PieceTradeReservation a, PieceTradeReservation b) {
        return Math.min(a.getRemainingQuantity(), b.getRemainingQuantity());
    }

    /** 자기 체결 방지 **/
    private boolean isSelfTrade(PieceTradeReservation a, PieceTradeReservation b) {
        return a.getMemberUuid().equals(b.getMemberUuid());
    }
}