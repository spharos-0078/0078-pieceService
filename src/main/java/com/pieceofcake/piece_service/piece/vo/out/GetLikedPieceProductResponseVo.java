package com.pieceofcake.piece_service.piece.vo.out;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GetLikedPieceProductResponseVo {
    private String pieceProductUuid;
    private String productUuid;
    private String memberUuid;

    @Builder
    public GetLikedPieceProductResponseVo(String pieceProductUuid, String productUuid, String memberUuid) {
        this.pieceProductUuid = pieceProductUuid;
        this.productUuid = productUuid;
        this.memberUuid = memberUuid;
    }
}
