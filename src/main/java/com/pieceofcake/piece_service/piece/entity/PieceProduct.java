package com.pieceofcake.piece_service.piece.entity;

import com.pieceofcake.piece_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Table(name = "piece_product")
@Getter
@NoArgsConstructor
@Entity
public class PieceProduct extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "piece_product_uuid", nullable = false, length = 50)
    private String pieceProductUuid;

    @Column(name = "product_uuid", nullable = false, length = 50)
    private String productUuid;

    @Column(name = "market_price")
    private Long marketPrice;

    @Column(name = "total_pieces")
    private Integer totalPieces;

    @Builder.Default
    @Column(name = "is_trading", nullable = false)
    Boolean isTrading = true;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    Boolean isDeleted = false;

    @Builder
    public PieceProduct(
            Long id, String pieceProductUuid, String productUuid,
            Long marketPrice, Integer totalPieces,
            Boolean isTrading, Boolean isDeleted) {
        this.id = id;
        this.pieceProductUuid = pieceProductUuid;
        this.productUuid = productUuid;
        this.marketPrice = marketPrice;
        this.totalPieces = totalPieces;
        this.isTrading = isTrading;
        this.isDeleted = isDeleted;
    }
}
