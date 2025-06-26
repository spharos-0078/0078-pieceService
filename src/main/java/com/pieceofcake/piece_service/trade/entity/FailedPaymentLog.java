package com.pieceofcake.piece_service.trade.entity;

import com.pieceofcake.piece_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class FailedPaymentLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String memberUuid;
    private String matchedUuid;
    private Long amount;
    private TradeType tradeType;

    private Integer retryCount;

    private LocalDateTime lastTriedAt;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Builder
    public FailedPaymentLog(
            Long id, String memberUuid, String matchedUuid, Long amount,
            TradeType tradeType, int retryCount,
            LocalDateTime lastTriedAt, PaymentStatus status
    ) {
        this.id = id;
        this.memberUuid = memberUuid;
        this.matchedUuid = matchedUuid;
        this.amount = amount;
        this.tradeType = tradeType;
        this.retryCount = retryCount;
        this.lastTriedAt = lastTriedAt;
        this.status = status;
    }

    public void increaseRetryCount() {
        this.retryCount++;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public void setLastTriedAt(LocalDateTime lastTriedAt) {
        this.lastTriedAt = lastTriedAt;
    }

    public static FailedPaymentLog of(String memberUuid, String matchedUuid, Long amount, PieceTradeReservation reservation) {
        return FailedPaymentLog.builder()
                .memberUuid(memberUuid)
                .matchedUuid(matchedUuid)
                .amount(amount)
                .tradeType(reservation.getTradeType())
                .retryCount(0)
                .lastTriedAt(LocalDateTime.now())
                .status(PaymentStatus.PENDING)
                .build();
    }
}
