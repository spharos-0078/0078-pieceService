package com.pieceofcake.piece_service.piece.entity;

import com.pieceofcake.piece_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "piece_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PieceProduct extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "piece_product_uuid", nullable = false, length = 50)
    private String pieceProductUuid;

    @Column(name = "product_uuid", nullable = false,length = 50)
    private String productUuid;

    @Column(name = "market_price")
    private Long marketPrice;

    @Column(name = "total_pieces")
    private Integer totalPieces;
}
