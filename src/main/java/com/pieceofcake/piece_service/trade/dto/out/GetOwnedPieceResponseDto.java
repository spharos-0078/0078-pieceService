package com.pieceofcake.piece_service.trade.dto.out;

import com.pieceofcake.piece_service.trade.vo.out.GetOwnedPieceResponseVo;
import lombok.Builder;
import lombok.Getter;

@Getter
public class GetOwnedPieceResponseDto {

    private String pieceProductUuid;
    private int pieceCount;

    @Builder
    public GetOwnedPieceResponseDto(
            String pieceProductUuid, int pieceCount
    ) {
        this.pieceProductUuid = pieceProductUuid;
        this.pieceCount = pieceCount;
    }

    public GetOwnedPieceResponseVo toVo() {
        return GetOwnedPieceResponseVo.builder()
                .pieceProductUuid(pieceProductUuid)
                .pieceCount(pieceCount)
                .build();
    }

}
