package com.pieceofcake.piece_service.piece.dto.in;

import com.pieceofcake.piece_service.piece.vo.in.CreatePieceRequestVo;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreatePieceRequestDto {
    private String productUuid;
    private Integer totalPieces;

    @Builder
    public CreatePieceRequestDto(String productUuid, Integer totalPieces) {
        this.productUuid = productUuid;
        this.totalPieces = totalPieces;
    }

    public static CreatePieceRequestDto from(CreatePieceRequestVo vo) {
        return CreatePieceRequestDto.builder()
                .productUuid(vo.getProductUuid())
                .totalPieces(vo.getTotalPieces())
                .build();
    }
}
