package com.pieceofcake.piece_service.trade.application;

import com.pieceofcake.piece_service.piece.infrastructure.PieceRepository;
import com.pieceofcake.piece_service.trade.dto.in.CreateTradedHistoryRequestDto;
import com.pieceofcake.piece_service.trade.dto.in.TransferPieceOwnershipRequestDto;
import com.pieceofcake.piece_service.trade.entity.*;
import com.pieceofcake.piece_service.trade.infrastructure.OwnedPieceRepository;
import com.pieceofcake.piece_service.trade.infrastructure.PieceMatchedHistoryRepository;
import com.pieceofcake.piece_service.trade.infrastructure.TradeReservationRepository;
import com.pieceofcake.piece_service.trade.infrastructure.TradedHistoryRepository;
import com.pieceofcake.piece_service.trade.infrastructure.feign.client.PaymentFeignClient;
import com.pieceofcake.piece_service.trade.infrastructure.feign.dto.CreateMoneyRequestFeignDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MatchingServiceImpl implements MatchingService {

    private final OwnedPieceRepository ownedPieceRepository;
    private final TradedHistoryRepository tradedHistoryRepository;
    private final TradeReservationRepository tradeReservationRepository;
    private final PaymentFeignClient paymentFeignClient;
    private final PieceRepository pieceRepository;
    private final PieceMatchedHistoryRepository pieceMatchedHistoryRepository;

    /* ============================================================================
       1. 외부에서 호출되는 진입 메서드 – ‘주문 1건’이 들어오면 즉시 매칭 시도
       ============================================================================ */
    @Transactional
    @Override
    public void match(PieceTradeReservation reservation) {
        // 1) 어떤 조각상품(pieceProductUuid)에 대한 주문인지 식별
        String pieceProductUuid = reservation.getPieceProductUuid();

        // ------------------------------------------------------
        // 2) 주문 종류(BUY / SELL)에 따라 대응 주문북을 조회 + 정렬
        // ------------------------------------------------------
        if (reservation.getTradeType() == TradeType.BUY) {
            /* ----- BUY 주문 : 매도 주문북 탐색 ----- */
            List<PieceTradeReservation> sellBook = tradeReservationRepository
                    .findWaitingByProductAndType(pieceProductUuid, TradeType.SELL, TradeStatus.WAITING);

            /* 낮은 가격 → 오래된 시간 → 잔량 많은 순서로 정렬 */
            Comparator<PieceTradeReservation> sellComparator =
                    Comparator.comparingLong(PieceTradeReservation::getRegisteredPrice)
                            .thenComparing(PieceTradeReservation::getCreatedAt)
                            .thenComparing(PieceTradeReservation::getRemainingQuantity, Comparator.reverseOrder());

            processMatching(sellBook, reservation, true, sellComparator);

        } else {
            /* ----- SELL 주문 : 매수 주문북 탐색 ----- */
            List<PieceTradeReservation> buyBook = tradeReservationRepository
                    .findWaitingByProductAndType(pieceProductUuid, TradeType.BUY, TradeStatus.WAITING);

            /* 높은 가격 → 오래된 시간 → 잔량 많은 순서로 정렬 */
            Comparator<PieceTradeReservation> buyComparator =
                    Comparator.comparingLong(PieceTradeReservation::getRegisteredPrice).reversed()
                            .thenComparing(PieceTradeReservation::getCreatedAt)
                            .thenComparing(PieceTradeReservation::getRemainingQuantity, Comparator.reverseOrder());

            processMatching(buyBook, reservation, false, buyComparator);
        }
    }

    /* ============================================================================
       2. 주문북(counterBook)을 돌면서 실제로 1건씩 체결을 시도하는 공통 로직
       ============================================================================ */
    private void processMatching(List<PieceTradeReservation> counterBook, // 반대 주문목록
                                 PieceTradeReservation reservation, // 현재 들어온 주문
                                 boolean isBuy, // true: 매수, false: 매도
                                 Comparator<PieceTradeReservation> comparator) {

        counterBook.sort(comparator); // 전달받은 기준으로 정렬

        /*
         * 정렬된 주문북을 순회하면서 아래 단계 수행
         *  ① 자기체결 방지
         *  ② 호가 비교 (가격 조건 통과 여부)
         *  ③ 체결 가능수량 산출
         *  ④ executeTrade(소유권 이전, 예치금 정산, 잔량 감소, 상태 업데이트)
         * 반복하면서 현재 주문(reservation)이 전부 소진되면 루프 탈출
         */
        for (PieceTradeReservation counter : counterBook) {
            // ① 본인 주문끼리 체결 금지
            if (isSelfTrade(reservation, counter)) continue;

            // ② 가격 조건 체크 (매수: 매도가 ≤ 매수가 / 매도: 매수가 ≥ 매도가)
            boolean priceCondition = isBuy
                    ? counter.getRegisteredPrice() <= reservation.getRegisteredPrice()
                    : counter.getRegisteredPrice() >= reservation.getRegisteredPrice();

            if (!priceCondition) break;

            // ③ 체결 가능 수량 산출
            int qty = calculateMatchQuantity(reservation, counter);
            long piecePrice = isBuy ? counter.getRegisteredPrice() : reservation.getRegisteredPrice();

            String matchedUuid = UUID.randomUUID().toString().substring(0, 32);
            saveMatchedHistory(reservation, matchedUuid, piecePrice, qty);

            // ④ 체결 실행
            if (isBuy) {
                executeTrade(reservation, counter, qty, piecePrice, matchedUuid); // reservation = buy
            } else {
                executeTrade(counter, reservation, qty, piecePrice, matchedUuid); // reservation = sell
            }

            // 남은 수량 0 → COMPLETED → 더 이상 반복 필요 X
            if (reservation.getTradeStatus() == TradeStatus.COMPLETED) break;
        }
    }

    /* ============================================================================
       3. executeTrade : 실제 체결(소유권·예치금·잔량·상태) 처리
       ============================================================================ */
    private void executeTrade(PieceTradeReservation buy, PieceTradeReservation sell, int matchQuantity, long piecePrice, String matchedUuid) {
        /* 3-1. 매도자 보유 조각 중 앞에서부터 matchQuantity 개 가져오기 */
        List<OwnedPiece> sellerPieces = ownedPieceRepository
                .findByMemberUuidAndPieceProductUuid(sell.getMemberUuid(), sell.getPieceProductUuid())
                .subList(0, matchQuantity);

        /* 3-2. 조각별로 소유권 이전 + 체결 이력 저장 */
        for (OwnedPiece piece : sellerPieces) {
            transferOwnership(piece, buy.getMemberUuid());

            saveTradeHistory(buy, sell, piece, matchedUuid);
        }

        // todo: 체결 시 Redis로 체결 내역 전달

        /* 3-3. 예치금 정산 (총 가격 = 체결가 × 체결 수량) */
        long totalPrice = matchQuantity * piecePrice;

        paymentFeignClient.createMoney(buy.getMemberUuid(), CreateMoneyRequestFeignDto.buy(totalPrice)); // 매수자 차감
        paymentFeignClient.createMoney(sell.getMemberUuid(), CreateMoneyRequestFeignDto.sell(totalPrice)); // 매도자 입금

        /* 3-4. 잔량 차감 및 상태 업데이트 */
        buy.reduceQuantity(matchQuantity);
        sell.reduceQuantity(matchQuantity);
        tradeReservationRepository.save(sell);
        tradeReservationRepository.save(buy);
    }

    /** 보유 조각 소유권 이전 */
    private void transferOwnership(OwnedPiece piece, String newOwnerUuid) {
        OwnedPiece newOwnedPiece = TransferPieceOwnershipRequestDto.of(piece, newOwnerUuid).toEntity();
        ownedPieceRepository.save(newOwnedPiece);
        ownedPieceRepository.delete(piece);
        pieceRepository.updateOwner(piece.getPieceUuid(), newOwnerUuid);
    }

    /** 체결 이력 저장 */
    private void saveTradeHistory(PieceTradeReservation buy, PieceTradeReservation sell, OwnedPiece piece, String matchedUuid) {
        tradedHistoryRepository.save(CreateTradedHistoryRequestDto.of(
                buy, piece.getPieceUuid(), TradeType.BUY, buy.getMemberUuid()).toEntity());

        tradedHistoryRepository.save(CreateTradedHistoryRequestDto.of(
                sell, piece.getPieceUuid(), TradeType.SELL, sell.getMemberUuid()).toEntity());
    }

    private void saveMatchedHistory(PieceTradeReservation reservation, String matchedUuid, long price, int qty) {
        PieceMatchedHistory matched = PieceMatchedHistory.builder()
                .matchedUuid(matchedUuid)
                .pieceProductUuid(reservation.getPieceProductUuid())
                .piecePrice(price)
                .matchedQuantity(qty)
                .matchedTime(LocalDateTime.now())
                .memberUuid(reservation.getMemberUuid())
                .tradeType(reservation.getTradeType())
                .build();
        pieceMatchedHistoryRepository.save(matched);
    }

    /** 가능한 체결 수량 = 두 주문의 ‘남은 수량’ 중 더 작은 값 */
    private int calculateMatchQuantity(PieceTradeReservation a, PieceTradeReservation b) {
        return Math.min(a.getRemainingQuantity(), b.getRemainingQuantity());
    }

    /** 자기 체결 방지 **/
    private boolean isSelfTrade(PieceTradeReservation a, PieceTradeReservation b) {
        return a.getMemberUuid().equals(b.getMemberUuid());
    }
}