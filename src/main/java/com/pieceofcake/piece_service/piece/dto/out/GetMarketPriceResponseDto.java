package com.pieceofcake.piece_service.piece.dto.out;

import com.pieceofcake.piece_service.piece.vo.out.GetMarketPriceResponseVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GetMarketPriceResponseDto {
    private String pieceProductUuid;
    private Long marketPrice;

    @Builder
    public GetMarketPriceResponseDto(String pieceProductUuid, Long marketPrice) {
        this.pieceProductUuid = pieceProductUuid;
        this.marketPrice = marketPrice;
    }

    public static GetMarketPriceResponseDto from(String pieceProductUuid, GetMarketPriceResponseVo getMarketPriceResponseVo) {
        return GetMarketPriceResponseDto.builder()
                .pieceProductUuid(pieceProductUuid)
                .marketPrice(getMarketPriceResponseVo.getMarketPrice())
                .build();
    }

    public GetMarketPriceResponseVo toVo() {
        return GetMarketPriceResponseVo.builder()
                .marketPrice(marketPrice)
                .build();
    }
}
