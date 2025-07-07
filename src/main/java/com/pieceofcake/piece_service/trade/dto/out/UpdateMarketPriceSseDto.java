package com.pieceofcake.piece_service.trade.dto.out;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@NoArgsConstructor
public class UpdateMarketPriceSseDto {
    @JsonProperty("piecePrice")
    private String marketPrice;

    @Builder
    public UpdateMarketPriceSseDto(String marketPrice) {
        this.marketPrice = marketPrice;
    }
}
