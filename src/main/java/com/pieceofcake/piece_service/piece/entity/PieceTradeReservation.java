package com.pieceofcake.piece_service.piece.entity;

import com.pieceofcake.piece_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
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
}
