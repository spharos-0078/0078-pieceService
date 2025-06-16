package com.pieceofcake.piece_service.trade.infrastructure.feign.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateMoneyRequestFeignDto {

    private Long price;
    private String moneyHistoryType;

    @Builder
    public CreateMoneyRequestFeignDto(Long price, String moneyHistoryType) {
        this.price = price;
        this.moneyHistoryType = moneyHistoryType;
    }

    public static CreateMoneyRequestFeignDto buy(Long totalPrice) {
        return CreateMoneyRequestFeignDto.builder()
                .price(totalPrice)
                .moneyHistoryType("FRACTION_BUY")
                .build();
    }

    public static CreateMoneyRequestFeignDto sell(Long totalPrice) {
        return CreateMoneyRequestFeignDto.builder()
                .price(totalPrice)
                .moneyHistoryType("FRACTION_SELL")
                .build();
    }

}
