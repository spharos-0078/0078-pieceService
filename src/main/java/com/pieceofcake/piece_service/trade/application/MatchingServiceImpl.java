package com.pieceofcake.piece_service.trade.application;

import com.pieceofcake.piece_service.piece.infrastructure.PieceProductRepository;
import com.pieceofcake.piece_service.piece.infrastructure.PieceRepository;
import com.pieceofcake.piece_service.trade.dto.in.CreateMatchedHistoryRequestDto;
import com.pieceofcake.piece_service.trade.dto.in.CreateTradedHistoryRequestDto;
import com.pieceofcake.piece_service.trade.dto.in.TransferPieceOwnershipRequestDto;
import com.pieceofcake.piece_service.trade.entity.*;
import com.pieceofcake.piece_service.trade.infrastructure.*;
import com.pieceofcake.piece_service.trade.infrastructure.feign.client.PaymentFeignClient;
import com.pieceofcake.piece_service.trade.infrastructure.feign.dto.CreateMoneyRequestFeignDto;
import com.pieceofcake.piece_service.trade.infrastructure.redis.RedisPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class MatchingServiceImpl implements MatchingService {

    private final OwnedPieceRepository ownedPieceRepository;
    private final TradedHistoryRepository tradedHistoryRepository;
    private final TradeReservationRepository tradeReservationRepository;
    private final PaymentFeignClient paymentFeignClient;
    private final PieceRepository pieceRepository;
    private final PieceMatchedHistoryRepository pieceMatchedHistoryRepository;

    private final RedisPublisher redisPublisher;
    private final RedissonClient redissonClient;

    private final FailedPaymentLogRepository failedPaymentLogRepository;
    private final OwnedPieceAverageRepository ownedPieceAverageRepository;
    private final PieceProductRepository pieceProductRepository;

    @Transactional
    @Override
    public void match(PieceTradeReservation reservation) {
        /* 동시에 여러 사용자가 동일한 조각 상품에 대해 주문을 시도
         * 데이터베이스에서는 동시성 제어가 복잡하고 무겁기 때문에, Redis로 락 처리
         */
        String lockKey = "lock:match:" + reservation.getPieceProductUuid();
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;

        try {
            locked = lock.tryLock(5, 3, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("체결 락 획득 실패: {}", lockKey);
                return;
            }
            doMatch(reservation);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("매칭 중단됨: {}", e.getMessage());
        } finally {
            if (locked) lock.unlock();
        }

    }

    private void doMatch(PieceTradeReservation reservation) {
        String pieceProductUuid = reservation.getPieceProductUuid();

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

    private void processMatching(List<PieceTradeReservation> counterBook,
                                 PieceTradeReservation reservation,
                                 boolean isBuy,
                                 Comparator<PieceTradeReservation> comparator) {

        counterBook.sort(comparator);

        /*
         * 정렬된 주문북을 순회하면서 아래 단계 수행
         *  ① 자기체결 방지
         *  ② 호가 비교 (가격 조건 통과 여부)
         *  ③ 체결 가능수량 산출
         *  ④ executeTrade(소유권 이전, 예치금 정산, 잔량 감소, 상태 업데이트)
         * 반복하면서 현재 주문(reservation)이 전부 소진되면 루프 탈출
         */
        for (PieceTradeReservation counter : counterBook) {
            if (isSelfTrade(reservation, counter)) continue;

            // 가격 조건 체크 (매수: 매도가 ≤ 매수가 / 매도: 매수가 ≥ 매도가)
            boolean priceCondition = isBuy
                    ? counter.getRegisteredPrice() <= reservation.getRegisteredPrice()
                    : counter.getRegisteredPrice() >= reservation.getRegisteredPrice();

            if (!priceCondition) break;

            // 체결 가능 수량 산출
            int matchQuantity = calculateMatchQuantity(reservation, counter);
            long piecePrice = isBuy ? counter.getRegisteredPrice() : reservation.getRegisteredPrice();
            LocalDateTime matchedTime = LocalDateTime.now();
            String matchedUuid = UUID.randomUUID().toString().substring(0, 32);

            saveMatchedHistory(reservation, counter, matchedUuid, piecePrice, matchQuantity, matchedTime);

            // 체결 실행
            if (isBuy) {
                executeTrade(reservation, counter, matchQuantity, piecePrice, matchedUuid); // reservation = buy
            } else {
                executeTrade(counter, reservation, matchQuantity, piecePrice, matchedUuid); // reservation = sell
            }

            if (reservation.getTradeStatus() == TradeStatus.COMPLETED) break;
        }
    }

    private void executeTrade(PieceTradeReservation buy, PieceTradeReservation sell,
                              int matchQuantity, long piecePrice, String matchedUuid
    ) {
        /* 1. 매도자 보유 조각 중 앞에서부터 matchQuantity 개 가져오기 */
        List<OwnedPiece> sellerPieces = ownedPieceRepository
                .findByMemberUuidAndPieceProductUuid(sell.getMemberUuid(), sell.getPieceProductUuid())
                .subList(0, matchQuantity);

        /* 2. 조각별로 소유권 이전 + 체결 이력 저장 */
        for (OwnedPiece piece : sellerPieces) {
            transferOwnership(piece, buy.getMemberUuid());
            saveTradeHistory(buy, sell, piece, matchedUuid);
        }

        /* 3. 예치금 정산 (총 가격 = 체결가 × 체결 수량) */
        long totalPrice = matchQuantity * piecePrice;

        try {
            paymentFeignClient.createMoney(buy.getMemberUuid(), CreateMoneyRequestFeignDto.buy(totalPrice));
        } catch (Exception e) {
            failedPaymentLogRepository.save(FailedPaymentLog.of(buy.getMemberUuid(), matchedUuid, totalPrice, buy));
            log.error("매수자 예치금 차감 실패: member={}, amount={}", buy.getMemberUuid(), totalPrice);
        }

        try {
            paymentFeignClient.createMoney(sell.getMemberUuid(), CreateMoneyRequestFeignDto.sell(totalPrice));
        } catch (Exception e) {
            failedPaymentLogRepository.save(FailedPaymentLog.of(sell.getMemberUuid(), matchedUuid, totalPrice, sell));
            log.error("매도자 예치금 입금 실패: member={}, amount={}", sell.getMemberUuid(), totalPrice);
        }

        updateAveragePrice(buy.getMemberUuid(), buy.getPieceProductUuid(), matchQuantity, piecePrice, TradeType.BUY);
        updateAveragePrice(sell.getMemberUuid(), sell.getPieceProductUuid(), matchQuantity, piecePrice, TradeType.SELL);

        /* 4. 잔량 차감 및 상태 업데이트 */
        buy.reduceQuantity(matchQuantity);
        sell.reduceQuantity(matchQuantity);
        tradeReservationRepository.saveAll(List.of(buy, sell));

        /* 5. Redis로 호가창 데이터 전송 (가격 기준 수량 누적) */
        redisPublisher.publishOrderBook(buy.getPieceProductUuid(), piecePrice, matchQuantity, TradeType.BUY.name());
        redisPublisher.publishOrderBook(sell.getPieceProductUuid(), piecePrice, matchQuantity, TradeType.SELL.name());

        /* 6. 조각 상품 테이블의 시장가 갱신 */
        pieceProductRepository.updateMarketPrice(sell.getPieceProductUuid(), piecePrice);
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

    private void saveMatchedHistory(
            PieceTradeReservation reservation, PieceTradeReservation counter,
            String matchedUuid, long piecePrice, int matchedQuantity, LocalDateTime matchedTime
    ) {
        PieceTradeReservation buyReservation = reservation.getTradeType() == TradeType.BUY ? reservation : counter;
        PieceTradeReservation sellReservation = reservation.getTradeType() == TradeType.SELL ? reservation : counter;

        pieceMatchedHistoryRepository.save(CreateMatchedHistoryRequestDto.of(
                buyReservation, matchedUuid, piecePrice, matchedQuantity, buyReservation.getMemberUuid(), TradeType.BUY).toEntity());

        pieceMatchedHistoryRepository.save(CreateMatchedHistoryRequestDto.of(
                sellReservation, matchedUuid, piecePrice, matchedQuantity, sellReservation.getMemberUuid(), TradeType.SELL).toEntity());

        redisPublisher.publishTradeVolume(
                reservation.getPieceProductUuid(), piecePrice, matchedQuantity, matchedTime
        );

        Map<String, Object> pubsubPayload = new HashMap<>();
        pubsubPayload.put("pieceProductUuid", reservation.getPieceProductUuid());
        pubsubPayload.put("piecePrice", piecePrice);
        pubsubPayload.put("matchedQuantity", matchedQuantity);
        pubsubPayload.put("matchedTime", matchedTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        redisPublisher.publishMatchedEvent(reservation.getPieceProductUuid(), pubsubPayload);
    }

    /** 가능한 체결 수량 = 두 주문의 ‘남은 수량’ 중 더 작은 값 */
    private int calculateMatchQuantity(PieceTradeReservation a, PieceTradeReservation b) {
        return Math.min(a.getRemainingQuantity(), b.getRemainingQuantity());
    }

    /** 자기 체결 방지 **/
    private boolean isSelfTrade(PieceTradeReservation a, PieceTradeReservation b) {
        return a.getMemberUuid().equals(b.getMemberUuid());
    }

    private void updateAveragePrice(String memberUuid,
                                    String pieceProductUuid,
                                    int qty,
                                    long pricePerPiece,
                                    TradeType tradeType) {

        OwnedPieceAverage avg = ownedPieceAverageRepository
                .findByMemberUuidAndPieceProductUuid(memberUuid, pieceProductUuid)
                .orElseGet(() -> OwnedPieceAverage.builder()
                        .memberUuid(memberUuid)
                        .pieceProductUuid(pieceProductUuid)
                        .totalQuantity(0)
                        .totalAmount(0L)
                        .averagePrice(0L)
                        .build());

        if (tradeType == TradeType.BUY) {
            avg.increase(qty, pricePerPiece);
        } else if (tradeType == TradeType.SELL) {
            avg.decrease(qty, pricePerPiece);
        }

        if (avg.getTotalQuantity() == 0) {
            ownedPieceAverageRepository.delete(avg);
            return;
        }

        ownedPieceAverageRepository.save(avg);
    }
}