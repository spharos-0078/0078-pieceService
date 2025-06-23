package com.pieceofcake.piece_service.trade.entity;

import com.pieceofcake.piece_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name = "piece_matched_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PieceMatchedHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "matched_uuid", nullable = false)
    private String matchedUuid;

    @Column(name = "piece_product_uuid", nullable = false)
    private String pieceProductUuid;

    @Column(name = "piece_price", nullable = false)
    private Long piecePrice;

    @Column(name = "matched_quantity", nullable = false)
    private Integer matchedQuantity;

    @Column(name = "matched_time", nullable = false)
    private LocalDateTime matchedTime;

    @Column(name = "member_uuid", nullable = false)
    private String memberUuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", nullable = false, length = 20)
    private TradeType tradeType;

    @Builder
    public PieceMatchedHistory(
            Long id, String matchedUuid, String pieceProductUuid,
            Long piecePrice, Integer matchedQuantity, LocalDateTime matchedTime,
            String memberUuid, TradeType tradeType
    ) {
        this.id = id;
        this.matchedUuid = matchedUuid;
        this.pieceProductUuid = pieceProductUuid;
        this.piecePrice = piecePrice;
        this.matchedQuantity = matchedQuantity;
        this.matchedTime = matchedTime;
        this.memberUuid = memberUuid;
        this.tradeType = tradeType;
    }
}
