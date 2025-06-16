package com.pieceofcake.piece_service.piece.dto.in;

import com.pieceofcake.piece_service.piece.entity.PieceProduct;
import com.pieceofcake.piece_service.piece.vo.in.CreatePieceProductRequestVo;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreatePieceProductRequestDto {
    private String productUuid;
    private Long marketPrice;

    @Builder
    public CreatePieceProductRequestDto(String productUuid, Long marketPrice) {
        this.productUuid = productUuid;
        this.marketPrice = marketPrice;
    }

    public static CreatePieceProductRequestDto from(CreatePieceProductRequestVo vo){
        return CreatePieceProductRequestDto.builder()
                .productUuid(vo.getProductUuid())
                .marketPrice(vo.getMarketPrice())
                .build();
    }

    public PieceProduct toEntity(){
        return PieceProduct.builder()
                .productUuid(productUuid)
                .marketPrice(marketPrice)
                .build();
    }
}
