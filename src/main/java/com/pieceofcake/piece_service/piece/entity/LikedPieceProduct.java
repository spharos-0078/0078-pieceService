package com.pieceofcake.piece_service.piece.entity;

import com.pieceofcake.piece_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class LikedPieceProduct extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    private String pieceProductUuid;
    private String productUuid;
    private String memberUuid;

    @Builder
    public LikedPieceProduct(
            Long id, String pieceProductUuid, String productUuid, String memberUuid
    ) {
        this.id = id;
        this.pieceProductUuid = pieceProductUuid;
        this.productUuid = productUuid;
        this.memberUuid = memberUuid;
    }
}
