package com.pieceofcake.piece_service.trade.vo.out;

import lombok.Builder;
import lombok.Getter;

@Getter
public class GetOwnedPieceResponseVo {

    private String pieceProductUuid;
    private int pieceCount;

    @Builder
    public GetOwnedPieceResponseVo(
            String pieceProductUuid, int pieceCount
    ) {
        this.pieceProductUuid = pieceProductUuid;
        this.pieceCount = pieceCount;
    }
}
