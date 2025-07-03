package com.pieceofcake.piece_service.piece.dto.in;

import com.pieceofcake.piece_service.piece.vo.in.CancelPieceRequestVo;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CancelPieceRequestDto {
    private String productUuid;
    private String memberUuid;

    @Builder
    public CancelPieceRequestDto(String productUuid, String memberUuid) {
        this.productUuid = productUuid;
        this.memberUuid = memberUuid;
    }

    public static CancelPieceRequestDto of(String memberUuid, CancelPieceRequestVo vo) {
        return CancelPieceRequestDto.builder()
                .productUuid(vo.getProductUuid())
                .memberUuid(memberUuid)
                .build();
    }
}
