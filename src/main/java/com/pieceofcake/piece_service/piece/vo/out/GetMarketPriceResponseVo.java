package com.pieceofcake.piece_service.piece.vo.out;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GetMarketPriceResponseVo {
    private Long marketPrice;

    @Builder
    public GetMarketPriceResponseVo(Long marketPrice) {
        this.marketPrice = marketPrice;
    }
}
