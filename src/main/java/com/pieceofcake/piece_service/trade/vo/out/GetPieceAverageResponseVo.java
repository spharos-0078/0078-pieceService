package com.pieceofcake.piece_service.trade.vo.out;

import lombok.Builder;
import lombok.Getter;

@Getter
public class GetPieceAverageResponseVo {
    private String pieceProductUuid;
    private Integer totalQuantity;
    private Long totalAmount;
    private Long averagePrice;

    @Builder
    public GetPieceAverageResponseVo(
            String pieceProductUuid, Integer totalQuantity,
            Long totalAmount, Long averagePrice
    ) {
        this.pieceProductUuid = pieceProductUuid;
        this.totalQuantity = totalQuantity;
        this.totalAmount = totalAmount;
        this.averagePrice = averagePrice;
    }
}
