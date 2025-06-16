package com.pieceofcake.piece_service.trade.dto.out;

import com.pieceofcake.piece_service.trade.vo.out.GetOwnedMemberAndPieceQuantityResponseVo;
import lombok.Builder;
import lombok.Getter;

@Getter
public class GetOwnedMemberAndPieceQuantityResponseDto {
    private String memberUuid;
    private Integer pieceQuantity;

    @Builder
    public GetOwnedMemberAndPieceQuantityResponseDto(String memberUuid, Integer pieceQuantity) {
        this.memberUuid = memberUuid;
        this.pieceQuantity = pieceQuantity;
    }

    public GetOwnedMemberAndPieceQuantityResponseVo toVo(){
        return GetOwnedMemberAndPieceQuantityResponseVo.builder()
                .memberUuid(memberUuid)
                .pieceQuantity(pieceQuantity)
                .build();
    }
}
