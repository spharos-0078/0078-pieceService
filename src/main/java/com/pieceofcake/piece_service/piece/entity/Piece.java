package com.pieceofcake.piece_service.piece.entity;

import com.pieceofcake.piece_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "piece")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Piece extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "piece_uuid", nullable = false, length = 50)
    private String pieceUuid;

    @Column(name = "product_uuid", nullable = false, length = 50)
    private String productUuid;

    @Column(name = "is_sold")
    private Boolean isSold;
}
