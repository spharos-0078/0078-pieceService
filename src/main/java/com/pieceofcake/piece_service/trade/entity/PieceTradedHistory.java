package com.pieceofcake.piece_service.trade.entity;

import com.pieceofcake.piece_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "piece_trade_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PieceTradedHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "matched_uuid", nullable = false)
    private String matchedUuid;

    @Column(name = "member_uuid", nullable = false, length = 50)
    private String memberUuid;

    @Column(name = "piece_uuid", nullable = false)
    private String pieceUuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", nullable = false, length = 20)
    private TradeType tradeType;

    @Builder
    public PieceTradedHistory(
            Long id, String matchedUuid, String memberUuid,
            String pieceUuid, TradeType tradeType
    ) {
        this.id = id;
        this.matchedUuid = matchedUuid;
        this.memberUuid = memberUuid;
        this.pieceUuid = pieceUuid;
        this.tradeType = tradeType;
    }
}
