package com.pieceofcake.piece_service.trade.infrastructure.feign.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateMoneyRequestFeignDto {

    private String memberUuid;
    private Long amount;
    private String historyType;
    private Boolean isPositive;

    @Builder
    public CreateMoneyRequestFeignDto(
            Long amount, String historyType,
            Boolean isPositive, String memberUuid
    ) {
        this.amount = amount;
        this.historyType = historyType;
        this.isPositive = isPositive;
        this.memberUuid = memberUuid;
    }

    public static CreateMoneyRequestFeignDto buy(String memberUuid, Long totalPrice) {
        return CreateMoneyRequestFeignDto.builder()
                .memberUuid(memberUuid)
                .amount(totalPrice)
                .isPositive(false)
                .historyType("FRACTION_BUY")
                .build();
    }

    public static CreateMoneyRequestFeignDto sell(String memberUuid, Long totalPrice) {
        return CreateMoneyRequestFeignDto.builder()
                .memberUuid(memberUuid)
                .amount(totalPrice)
                .isPositive(true)
                .historyType("FRACTION_SELL")
                .build();
    }

}
