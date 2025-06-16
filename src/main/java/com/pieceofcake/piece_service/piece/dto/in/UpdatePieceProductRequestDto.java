package com.pieceofcake.piece_service.piece.dto.in;

import com.pieceofcake.piece_service.piece.entity.PieceProduct;
import com.pieceofcake.piece_service.piece.vo.in.UpdatePieceProductRequestVo;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UpdatePieceProductRequestDto {
    private String pieceProductUuid;
    private String productUuid;
    private Long marketPrice;
    private Boolean isTrading;

    @Builder
    public UpdatePieceProductRequestDto(String pieceProductUuid, String productUuid, Long marketPrice, Boolean isTrading) {
        this.pieceProductUuid = pieceProductUuid;
        this.productUuid = productUuid;
        this.marketPrice = marketPrice;
        this.isTrading = isTrading;
    }

    public static UpdatePieceProductRequestDto from(UpdatePieceProductRequestVo vo){
        return UpdatePieceProductRequestDto.builder()
                .pieceProductUuid(vo.getPieceProductUuid())
                .productUuid(vo.getProductUuid())
                .marketPrice(vo.getMarketPrice())
                .isTrading(vo.getIsTrading())
                .build();
    }

    public PieceProduct toEntity(PieceProduct pieceProduct){
        return PieceProduct.builder()
                .pieceProductUuid(pieceProduct.getPieceProductUuid())
                .productUuid(productUuid == null ? pieceProduct.getProductUuid() : productUuid)
                .marketPrice(marketPrice == null ? pieceProduct.getMarketPrice() : marketPrice)
                .isTrading(isTrading == null ? pieceProduct.getIsTrading() : isTrading)
                .build();
    }
}
