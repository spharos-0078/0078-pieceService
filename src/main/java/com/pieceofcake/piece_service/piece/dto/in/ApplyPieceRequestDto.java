package com.pieceofcake.piece_service.piece.dto.in;

import com.pieceofcake.piece_service.piece.vo.in.ApplyPieceRequestVo;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ApplyPieceRequestDto {
    private String productUuid;
    private Integer pieceQuantity;
    private String memberUuid;

    @Builder
    public ApplyPieceRequestDto(String productUuid, Integer pieceQuantity, String memberUuid) {
        this.productUuid = productUuid;
        this.pieceQuantity = pieceQuantity;
        this.memberUuid = memberUuid;
    }

    public static ApplyPieceRequestDto of(String memberUuid, ApplyPieceRequestVo vo) {
        return ApplyPieceRequestDto.builder()
                .productUuid(vo.getProductUuid())
                .pieceQuantity(vo.getPieceQuantity())
                .memberUuid(memberUuid)
                .build();
    }
}
