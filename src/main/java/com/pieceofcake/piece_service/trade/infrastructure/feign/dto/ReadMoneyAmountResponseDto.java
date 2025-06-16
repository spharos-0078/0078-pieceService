package com.pieceofcake.piece_service.trade.infrastructure.feign.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ReadMoneyAmountResponseDto {

    private Long amount;

    @Builder
    public ReadMoneyAmountResponseDto(Long amount) {
        this.amount = amount;
    }

    public Long getAmount() {
        return amount;
    }
}
