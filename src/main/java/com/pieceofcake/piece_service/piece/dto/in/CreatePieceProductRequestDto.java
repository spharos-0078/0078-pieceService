package com.pieceofcake.piece_service.piece.dto.in;

import com.pieceofcake.piece_service.piece.entity.PieceProduct;
import com.pieceofcake.piece_service.piece.vo.in.CreatePieceProductRequestVo;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreatePieceProductRequestDto {
    private String productUuid;
    private Integer totalPieces;
    private Long marketPrice;

    @Builder
    public CreatePieceProductRequestDto(String productUuid, Long marketPrice, Integer totalPieces) {
        this.productUuid = productUuid;
        this.marketPrice = marketPrice;
        this.totalPieces = totalPieces;
    }

    public static CreatePieceProductRequestDto from(CreatePieceProductRequestVo vo){
        return CreatePieceProductRequestDto.builder()
                .productUuid(vo.getProductUuid())
                .marketPrice(vo.getMarketPrice())
                .totalPieces(vo.getTotalPieces())
                .build();
    }

    public static CreatePieceProductRequestDto from(String productUuid, Long marketPrice, Integer totalPieces){
        return CreatePieceProductRequestDto.builder()
                .productUuid(productUuid)
                .marketPrice(marketPrice)
                .totalPieces(totalPieces)
                .build();
    }

    public PieceProduct toEntity(String pieceProductUuid){
        return PieceProduct.builder()
                .pieceProductUuid(pieceProductUuid)
                .productUuid(productUuid)
                .marketPrice(marketPrice)
                .build();
    }
}
