package com.pieceofcake.piece_service.trade.infrastructure.feign.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ReadMoneyAmountResponseDto {

    private Long remainingMoney;

    @Builder
    public ReadMoneyAmountResponseDto(Long remainingMoney) {
        this.remainingMoney = remainingMoney;
    }
}
