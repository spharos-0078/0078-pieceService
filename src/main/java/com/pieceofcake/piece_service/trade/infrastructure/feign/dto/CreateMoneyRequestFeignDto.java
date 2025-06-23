package com.pieceofcake.piece_service.trade.infrastructure.feign.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CreateMoneyRequestFeignDto {

    private Long amount;
    private String historyType;
    private Boolean isPositive;
    private String moneyHistoryDetail;
    private String bankName;
    private String accountNumber;
    private String accountHolderName;
    private String paymentUuid;
    private LocalDateTime paymentTime;
    private String paymentMethod;
    private String paymentStatus;

    @Builder
    public CreateMoneyRequestFeignDto(
            Long amount, String historyType, Boolean isPositive, String moneyHistoryDetail,
            String bankName, String accountNumber, String accountHolderName, String paymentUuid,
            LocalDateTime paymentTime, String paymentMethod, String paymentStatus
    ) {
        this.amount = amount;
        this.historyType = historyType;
        this.isPositive = isPositive;
        this.moneyHistoryDetail = moneyHistoryDetail;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.paymentUuid = paymentUuid;
        this.paymentTime = paymentTime;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
    }

    public static CreateMoneyRequestFeignDto buy(Long totalPrice) {
        return CreateMoneyRequestFeignDto.builder()
                .amount(totalPrice)
                .isPositive(false)
                .historyType("FRACTION_BUY")
                .build();
    }

    public static CreateMoneyRequestFeignDto sell(Long totalPrice) {
        return CreateMoneyRequestFeignDto.builder()
                .amount(totalPrice)
                .isPositive(true)
                .historyType("FRACTION_SELL")
                .build();
    }

}
