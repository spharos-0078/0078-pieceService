package com.pieceofcake.piece_service.trade.application;

import com.pieceofcake.piece_service.piece.infrastructure.PieceProductRepository;
import com.pieceofcake.piece_service.trade.application.domain.PaymentService;
import com.pieceofcake.piece_service.trade.application.domain.TradeExecutionService;
import com.pieceofcake.piece_service.trade.application.domain.TradeHistoryService;
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
import java.util.concurrent.CompletableFuture;
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

    // 도메인 서비스들
    private final TradeExecutionService tradeExecutionService;
    private final PaymentService paymentService;
    private final TradeHistoryService tradeHistoryService;

    @Override
    public void match(PieceTradeReservation reservation) {
        String lockKey = "lock:match:" + reservation.getPieceProductUuid();
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;

        try {
            locked = lock.tryLock(5, 3, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("체결 락 획득 실패: {}", lockKey);
                return;
            }
            
            // 매칭 로직 실행 (트랜잭션 외부)
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
            processBuyOrder(reservation);
        } else {
            processSellOrder(reservation);
        }
    }

    private void processBuyOrder(PieceTradeReservation buyReservation) {
        List<PieceTradeReservation> sellBook = tradeReservationRepository
                .findWaitingByProductAndType(buyReservation.getPieceProductUuid(), TradeType.SELL, TradeStatus.WAITING);

        Comparator<PieceTradeReservation> sellComparator =
                Comparator.comparingLong(PieceTradeReservation::getRegisteredPrice)
                        .thenComparing(PieceTradeReservation::getCreatedAt)
                        .thenComparing(PieceTradeReservation::getRemainingQuantity, Comparator.reverseOrder());

        processMatching(sellBook, buyReservation, true, sellComparator);
    }

    private void processSellOrder(PieceTradeReservation sellReservation) {
        List<PieceTradeReservation> buyBook = tradeReservationRepository
                .findWaitingByProductAndType(sellReservation.getPieceProductUuid(), TradeType.BUY, TradeStatus.WAITING);

        Comparator<PieceTradeReservation> buyComparator =
                Comparator.comparingLong(PieceTradeReservation::getRegisteredPrice).reversed()
                        .thenComparing(PieceTradeReservation::getCreatedAt)
                        .thenComparing(PieceTradeReservation::getRemainingQuantity, Comparator.reverseOrder());

        processMatching(buyBook, sellReservation, false, buyComparator);
    }

    private void processMatching(List<PieceTradeReservation> counterBook,
                                 PieceTradeReservation reservation,
                                 boolean isBuy,
                                 Comparator<PieceTradeReservation> comparator) {

        counterBook.sort(comparator);

        for (PieceTradeReservation counter : counterBook) {
            if (isSelfTrade(reservation, counter)) continue;

            // 가격 조건 체크
            boolean priceCondition = isBuy
                    ? counter.getRegisteredPrice() <= reservation.getRegisteredPrice()
                    : counter.getRegisteredPrice() >= reservation.getRegisteredPrice();

            if (!priceCondition) break;

            // 체결 가능 수량 산출
            int matchQuantity = calculateMatchQuantity(reservation, counter);
            long piecePrice = isBuy ? counter.getRegisteredPrice() : reservation.getRegisteredPrice();
            LocalDateTime matchedTime = LocalDateTime.now();
            String matchedUuid = UUID.randomUUID().toString().substring(0, 32);

            // 체결 실행 (트랜잭션 내부)
            executeTradeWithTransaction(reservation, counter, matchQuantity, piecePrice, matchedUuid, matchedTime);

            if (reservation.getTradeStatus() == TradeStatus.COMPLETED) break;
        }
    }

    @Transactional
    public void executeTradeWithTransaction(PieceTradeReservation reservation, PieceTradeReservation counter,
                                           int matchQuantity, long piecePrice, String matchedUuid, LocalDateTime matchedTime) {
        
        PieceTradeReservation buy = reservation.getTradeType() == TradeType.BUY ? reservation : counter;
        PieceTradeReservation sell = reservation.getTradeType() == TradeType.SELL ? reservation : counter;

        // 1. 매도자 보유 조각 조회 (락을 걸고)
        List<OwnedPiece> sellerPieces = tradeExecutionService.getSellerPiecesWithLock(
                sell.getMemberUuid(), sell.getPieceProductUuid(), matchQuantity);

        // 2. 소유권 이전
        tradeExecutionService.transferOwnership(sellerPieces, buy.getMemberUuid());

        // 3. 거래 이력 저장
        tradeHistoryService.saveTradeHistory(buy, sell, sellerPieces, matchedUuid);
        tradeHistoryService.saveMatchedHistory(reservation, counter, matchedUuid, piecePrice, matchQuantity, matchedTime);

        // 4. 평균 가격 업데이트
        updateAveragePrice(buy.getMemberUuid(), buy.getPieceProductUuid(), matchQuantity, piecePrice, TradeType.BUY);
        updateAveragePrice(sell.getMemberUuid(), sell.getPieceProductUuid(), matchQuantity, piecePrice, TradeType.SELL);

        // 5. 잔량 차감 및 상태 업데이트
        buy.reduceQuantity(matchQuantity);
        sell.reduceQuantity(matchQuantity);
        tradeReservationRepository.saveAll(List.of(buy, sell));

        // 6. 시장가 갱신
        pieceProductRepository.updateMarketPrice(sell.getPieceProductUuid(), piecePrice);

        // 7. Redis 이벤트 발행
        publishOrderBookEvents(buy.getPieceProductUuid(), piecePrice, matchQuantity);

        // 8. 비동기 결제 처리 (트랜잭션 외부)
        long totalPrice = matchQuantity * piecePrice;
        CompletableFuture<Void> paymentFuture = paymentService.processPaymentAsync(buy, sell, totalPrice, matchedUuid);
        
        // 결제 완료 대기 (선택사항)
        paymentFuture.join();
    }

    private void publishOrderBookEvents(String pieceProductUuid, long piecePrice, int matchQuantity) {
        redisPublisher.publishOrderBook(pieceProductUuid, piecePrice, matchQuantity, TradeType.BUY.name());
        redisPublisher.publishOrderBook(pieceProductUuid, piecePrice, matchQuantity, TradeType.SELL.name());
    }

    private int calculateMatchQuantity(PieceTradeReservation a, PieceTradeReservation b) {
        return Math.min(a.getRemainingQuantity(), b.getRemainingQuantity());
    }

    private boolean isSelfTrade(PieceTradeReservation a, PieceTradeReservation b) {
        return a.getMemberUuid().equals(b.getMemberUuid());
    }

    @Transactional
    private void updateAveragePrice(String memberUuid, String pieceProductUuid, int qty, 
                                   long pricePerPiece, TradeType tradeType) {

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