package com.pieceofcake.piece_service.trade.vo.out;

import lombok.Builder;
import lombok.Getter;

@Getter
public class GetOwnedMemberAndPieceQuantityResponseVo {
    private String memberUuid;
    private Integer pieceQuantity;

    @Builder
    public GetOwnedMemberAndPieceQuantityResponseVo(String memberUuid, Integer pieceQuantity) {
        this.memberUuid = memberUuid;
        this.pieceQuantity = pieceQuantity;
    }
}
