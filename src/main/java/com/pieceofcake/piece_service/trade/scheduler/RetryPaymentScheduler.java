package com.pieceofcake.piece_service.trade.scheduler;

import com.pieceofcake.piece_service.trade.entity.FailedPaymentLog;
import com.pieceofcake.piece_service.trade.entity.PaymentStatus;
import com.pieceofcake.piece_service.trade.infrastructure.FailedPaymentLogRepository;
import com.pieceofcake.piece_service.trade.infrastructure.feign.client.PaymentFeignClient;
import com.pieceofcake.piece_service.trade.infrastructure.feign.dto.CreateMoneyRequestFeignDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryPaymentScheduler {

    private final FailedPaymentLogRepository failedPaymentLogRepository;
    private final PaymentFeignClient paymentFeignClient;

    private static final int MAX_RETRY_COUNT = 5;

    @Scheduled(fixedDelay = 60000) // 매 60초마다 재시도
    @Transactional
    public void retryFailedPayments() {
        List<FailedPaymentLog> failedList = failedPaymentLogRepository.findByStatus(PaymentStatus.PENDING);

        for (FailedPaymentLog paymentLog : failedList) {
            if (paymentLog.getRetryCount() >= MAX_RETRY_COUNT) {
                paymentLog.setStatus(PaymentStatus.FAILED);
                failedPaymentLogRepository.save(paymentLog);
                log.warn("[RetryPaymentScheduler] 최대 재시도 초과 - ID: {}, 상태 변경: FAIL", paymentLog.getId());
                continue;
            }

            try {
                paymentFeignClient.createMoney(paymentLog.getMemberUuid(),
                        paymentLog.getTradeType().equals(com.pieceofcake.piece_service.trade.entity.TradeType.BUY)
                                ? CreateMoneyRequestFeignDto.buy(paymentLog.getAmount())
                                : CreateMoneyRequestFeignDto.sell(paymentLog.getAmount()));

                paymentLog.setStatus(PaymentStatus.SUCCESS);
                paymentLog.setLastTriedAt(LocalDateTime.now());
                paymentLog.increaseRetryCount();

            } catch (Exception e) {
                paymentLog.setStatus(PaymentStatus.PENDING);
                paymentLog.setLastTriedAt(LocalDateTime.now());
                paymentLog.increaseRetryCount();
                log.warn("[RetryPaymentScheduler] 결제 재시도 실패 - {}", paymentLog.getId());
            }
        }
    }
}
