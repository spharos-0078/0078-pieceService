package com.pieceofcake.piece_service.trade.entity;

import com.pieceofcake.piece_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "piece_trade_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PieceTransactionHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "history_uuid", nullable = false, length = 50)
    private String historyUuid;

    @Column(name = "piece_product_uuid", nullable = false, length = 50)
    private String pieceProductUuid;

    @Column(name = "member_uuid", nullable = false, length = 50)
    private String memberUuid;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "piece_uuid", nullable = false, length = 50)
    private String pieceUuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", nullable = false, length = 20)
    private TradeType tradeType;
}
