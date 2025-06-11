package com.pieceofcake.piece_service.piece.vo.in;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CreatePieceRequestVo {
    private String productUuid;
    private Integer totalPieces;

    @Builder
    public CreatePieceRequestVo(String productUuid, Integer totalPieces) {
        this.productUuid = productUuid;
        this.totalPieces = totalPieces;
    }
}
