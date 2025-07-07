package com.pieceofcake.piece_service.trade.dto.out;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@NoArgsConstructor
public class MarketPriceSerializeDto {
    private String piecePrice;

    @Builder
    public MarketPriceSerializeDto(String piecePrice) {
        this.piecePrice = piecePrice;
    }
}
