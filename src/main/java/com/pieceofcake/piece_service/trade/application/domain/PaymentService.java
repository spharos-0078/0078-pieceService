package com.pieceofcake.piece_service.trade.application.domain;

import com.pieceofcake.piece_service.trade.entity.PieceTradeReservation;
import com.pieceofcake.piece_service.trade.infrastructure.FailedPaymentLogRepository;
import com.pieceofcake.piece_service.trade.infrastructure.feign.client.PaymentFeignClient;
import com.pieceofcake.piece_service.trade.infrastructure.feign.dto.CreateMoneyRequestFeignDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentFeignClient paymentFeignClient;
    private final FailedPaymentLogRepository failedPaymentLogRepository;
    
    /**
     * 비동기 결제 처리
     * 트랜잭션 외부에서 실행되어 롤백 위험을 방지
     */
    public CompletableFuture<Void> processPaymentAsync(
            PieceTradeReservation buy, 
            PieceTradeReservation sell, 
            long totalPrice, 
            String matchedUuid) {
        
        return CompletableFuture.runAsync(() -> {
            try {
                // 매수자 예치금 차감
                processBuyerPayment(buy.getMemberUuid(), totalPrice, matchedUuid);
                
                // 매도자 예치금 입금
                processSellerPayment(sell.getMemberUuid(), totalPrice, matchedUuid);
                
                log.info("결제 처리 완료: matchedUuid={}, totalPrice={}", matchedUuid, totalPrice);
                
            } catch (Exception e) {
                log.error("결제 처리 실패: matchedUuid={}, error={}", matchedUuid, e.getMessage());
                // 실패 로그 저장
                saveFailedPaymentLog(buy.getMemberUuid(), matchedUuid, totalPrice, buy);
            }
        });
    }
    
    private void processBuyerPayment(String memberUuid, long amount, String matchedUuid) {
        try {
            paymentFeignClient.createMoney(memberUuid, CreateMoneyRequestFeignDto.buy(amount));
            log.info("매수자 결제 성공: member={}, amount={}", memberUuid, amount);
        } catch (Exception e) {
            log.error("매수자 결제 실패: member={}, amount={}", memberUuid, amount, e);
            throw e;
        }
    }
    
    private void processSellerPayment(String memberUuid, long amount, String matchedUuid) {
        try {
            paymentFeignClient.createMoney(memberUuid, CreateMoneyRequestFeignDto.sell(amount));
            log.info("매도자 결제 성공: member={}, amount={}", memberUuid, amount);
        } catch (Exception e) {
            log.error("매도자 결제 실패: member={}, amount={}", memberUuid, amount, e);
            throw e;
        }
    }
    
    private void saveFailedPaymentLog(String memberUuid, String matchedUuid, long amount, PieceTradeReservation reservation) {
        try {
            failedPaymentLogRepository.save(
                com.pieceofcake.piece_service.trade.entity.FailedPaymentLog.of(memberUuid, matchedUuid, amount, reservation)
            );
        } catch (Exception e) {
            log.error("실패 결제 로그 저장 실패: member={}, matchedUuid={}", memberUuid, matchedUuid, e);
        }
    }
} 