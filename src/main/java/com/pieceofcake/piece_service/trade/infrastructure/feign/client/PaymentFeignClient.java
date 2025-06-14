package com.pieceofcake.piece_service.trade.infrastructure.feign.client;

import com.pieceofcake.piece_service.trade.infrastructure.feign.dto.CreateMoneyRequestFeignDto;
import com.pieceofcake.piece_service.trade.infrastructure.feign.dto.ReadMoneyAmountResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "payment-service", url = "${EC2_HOST}:8000/payment-service/api/v1")
public interface PaymentFeignClient {

    @PostMapping("/money")
    void createMoney(
            @RequestHeader("X-Member-Uuid") String memberUuid,
            @RequestBody CreateMoneyRequestFeignDto createMoneyRequestFeignDto
    );

    @GetMapping("/money")
    ReadMoneyAmountResponseDto getMoney(@RequestHeader("X-Member-Uuid") String memberUuid);

}
