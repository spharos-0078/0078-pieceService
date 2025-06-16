package com.pieceofcake.piece_service.trade.entity;

import com.pieceofcake.piece_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "owned_piece")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class OwnedPiece extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "owned_piece_uuid", nullable = false, length = 50)
    private String ownedPieceUuid;

    @Column(name = "piece_product_uuid", nullable = false, length = 50)
    private String pieceProductUuid;

    @Column(name = "member_uuid", nullable = false, length = 50)
    private String memberUuid;

    @Column(name = "piece_uuid", nullable = false, length = 50)
    private String pieceUuid;

    @Column(name = "trade_price")
    private Long tradePrice;

    @Builder
    public OwnedPiece(
            Long id, String ownedPieceUuid, String pieceProductUuid, String memberUuid,
            String pieceUuid, Long tradePrice
    ) {
        this.id = id;
        this.ownedPieceUuid = ownedPieceUuid;
        this.pieceProductUuid = pieceProductUuid;
        this.memberUuid = memberUuid;
        this.pieceUuid = pieceUuid;
        this.tradePrice = tradePrice;
    }
}