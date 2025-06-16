package com.pieceofcake.piece_service.trade.dto.out;

import com.pieceofcake.piece_service.trade.entity.OwnedPiece;
import com.pieceofcake.piece_service.trade.vo.out.GetAllPieceProductUuidResponseVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GetAllPieceProductUuidResponseDto {

    private String pieceProductUuid;

    @Builder
    public GetAllPieceProductUuidResponseDto(String pieceProductUuid) {
        this.pieceProductUuid = pieceProductUuid;
    }

    public static GetAllPieceProductUuidResponseDto from(OwnedPiece ownedPiece) {
        return GetAllPieceProductUuidResponseDto.builder()
                .pieceProductUuid(ownedPiece.getPieceProductUuid())
                .build();
    }

    public GetAllPieceProductUuidResponseVo toVo() {
        return GetAllPieceProductUuidResponseVo.builder()
                .pieceProductUuid(pieceProductUuid)
                .build();
    }
}
