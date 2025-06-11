package com.pieceofcake.piece_service.trade.presentation;

import com.pieceofcake.piece_service.common.entity.BaseResponseEntity;
import com.pieceofcake.piece_service.common.entity.BaseResponseStatus;
import com.pieceofcake.piece_service.trade.application.OwnedPieceService;
import com.pieceofcake.piece_service.trade.application.TradeReservationService;
import com.pieceofcake.piece_service.trade.dto.in.CreateTradeReservationRequestDto;
import com.pieceofcake.piece_service.trade.vo.in.CreateTradeReservationReqtestVo;
import com.pieceofcake.piece_service.trade.vo.out.CreateTradeReservationResponseVo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/vi/piece-trade")
@RequiredArgsConstructor
@RestController
public class TradeReservationController {

    private final TradeReservationService tradeReservationService;

    @Operation(summary = "조각 매수 예약 생성")
    @PostMapping("/buy")
    public BaseResponseEntity<CreateTradeReservationResponseVo> createTradeReservation(
            @RequestHeader("X-Member-Uuid") String memberUuid,
            @RequestBody CreateTradeReservationReqtestVo createTradeReservationReqtestVo
    ) {
        tradeReservationService.createTradeReservation(CreateTradeReservationRequestDto.from(createTradeReservationReqtestVo, memberUuid));
        return new BaseResponseEntity<>(new CreateTradeReservationResponseVo());
    }

}
