package com.pieceofcake.piece_service.trade.entity;

import com.pieceofcake.piece_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "piece_trade_reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PieceTradeReservation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "reservation_uuid", length = 50, nullable = false)
    private String reservationUuid;

    @Column(name = "piece_product_uuid", length = 50, nullable = false)
    private String pieceProductUuid;

    @Column(name = "member_uuid", length = 50, nullable = false)
    private String memberUuid;

    @Column(name = "registered_price", nullable = false)
    private Long registeredPrice;

    @Column(name = "desired_quantity", nullable = false)
    private Integer desiredQuantity;

    @Column(name = "remaining_quantity", nullable = false)
    private Integer remainingQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", length = 20, nullable = false)
    private TradeType tradeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_status", length = 20, nullable = false)
    private TradeStatus tradeStatus;

    @Builder
    public PieceTradeReservation(
            Long id, String reservationUuid, String pieceProductUuid, String memberUuid,
            Long registeredPrice, Integer desiredQuantity, Integer remainingQuantity,
            TradeType tradeType, TradeStatus tradeStatus
    ) {
        this.id = id;
        this.reservationUuid = reservationUuid;
        this.pieceProductUuid = pieceProductUuid;
        this.memberUuid = memberUuid;
        this.registeredPrice = registeredPrice;
        this.desiredQuantity = desiredQuantity;
        this.remainingQuantity = remainingQuantity;
        this.tradeType = tradeType;
        this.tradeStatus = tradeStatus;
    }

    public void reduceQuantity(int quantity) {
        this.remainingQuantity -= quantity;
        if (this.remainingQuantity <= 0) {
            this.remainingQuantity = 0;
            this.tradeStatus = TradeStatus.COMPLETED;
        }
    }

    public boolean isFullMatched() {
        return this.remainingQuantity == 0;
    }
}
