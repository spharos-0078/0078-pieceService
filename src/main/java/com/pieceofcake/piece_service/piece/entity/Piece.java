package com.pieceofcake.piece_service.piece.entity;

import com.pieceofcake.piece_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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

    @Column(name = "serial_number", nullable = false)
    private Integer serialNumber;

    @Column(name = "product_uuid", nullable = false, length = 50)
    private String productUuid;

    @Column(name = "member_uuid", length = 50)
    private String memberUuid;

    @Builder
    public Piece(Long id, String pieceUuid, Integer serialNumber, String productUuid, String memberUuid) {
        this.id = id;
        this.pieceUuid = pieceUuid;
        this.serialNumber = serialNumber;
        this.productUuid = productUuid;
        this.memberUuid = memberUuid;
    }
}
