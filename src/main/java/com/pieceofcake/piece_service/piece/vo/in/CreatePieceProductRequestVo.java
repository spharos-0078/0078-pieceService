package com.pieceofcake.piece_service.piece.vo.in;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CreatePieceProductRequestVo {
    private String productUuid;
    private Long marketPrice;
    private Integer totalPieces;

    @Builder
    public CreatePieceProductRequestVo(String productUuid, Long marketPrice, Integer totalPieces) {
        this.productUuid = productUuid;
        this.marketPrice = marketPrice;
        this.totalPieces = totalPieces;
    }
}
