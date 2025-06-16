package com.pieceofcake.piece_service.piece.vo.in;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UpdatePieceProductRequestVo {
    private String pieceProductUuid;
    private String productUuid;
    private Long marketPrice;
    private Boolean isTrading;

    @Builder
    public UpdatePieceProductRequestVo(String pieceProductUuid, String productUuid, Long marketPrice, Boolean isTrading) {
        this.pieceProductUuid = pieceProductUuid;
        this.productUuid = productUuid;
        this.marketPrice = marketPrice;
        this.isTrading = isTrading;
    }
}
