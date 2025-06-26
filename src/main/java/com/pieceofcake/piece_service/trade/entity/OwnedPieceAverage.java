package com.pieceofcake.piece_service.trade.entity;

import com.pieceofcake.piece_service.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class OwnedPieceAverage  extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String memberUuid;
    private String pieceProductUuid;

    private Integer totalQuantity;
    private Long totalAmount;
    private Long averagePrice;

    @Builder
    public OwnedPieceAverage(
            Long id, String memberUuid, String pieceProductUuid,
            Integer totalQuantity, Long totalAmount, Long averagePrice
    ) {
        this.id = id;
        this.memberUuid = memberUuid;
        this.pieceProductUuid = pieceProductUuid;
        this.totalQuantity = totalQuantity;
        this.totalAmount = totalAmount;
        this.averagePrice = averagePrice;
    }

    public void increase(int qty, long pricePerPiece) {
        this.totalQuantity += qty;
        this.totalAmount += qty * pricePerPiece;
        this.averagePrice = totalQuantity == 0 ? 0 : totalAmount / totalQuantity;
    }

    public void decrease(int qty, long pricePerPiece) {
        if (this.totalQuantity < qty) {
            throw new IllegalArgumentException("잔여 수량보다 많은 조각을 매도할 수 없습니다.");
        }

        this.totalQuantity -= qty;
        this.totalAmount -= qty * pricePerPiece;
        this.averagePrice = totalQuantity == 0 ? 0 : totalAmount / totalQuantity;
    }
}
