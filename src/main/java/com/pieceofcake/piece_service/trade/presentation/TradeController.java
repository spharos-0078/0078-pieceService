package com.pieceofcake.piece_service.trade.presentation;

import com.pieceofcake.piece_service.common.entity.BaseResponseEntity;
import com.pieceofcake.piece_service.common.entity.BaseResponseStatus;
import com.pieceofcake.piece_service.trade.application.TradeService;
import com.pieceofcake.piece_service.trade.dto.in.CreateTradeRequestDto;
import com.pieceofcake.piece_service.trade.dto.out.GetAllPieceProductUuidResponseDto;
import com.pieceofcake.piece_service.trade.dto.out.GetOwnedPieceResponseDto;
import com.pieceofcake.piece_service.trade.vo.in.CreateTradeRequestVo;
import com.pieceofcake.piece_service.trade.vo.out.GetOwnedPieceResponseVo;
import com.pieceofcake.piece_service.trade.vo.out.GetAllPieceProductUuidResponseVo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("api/v1/piece")
@RequiredArgsConstructor
@RestController
public class TradeController {

    private final TradeService tradeService;

    @Operation(summary = "멤버별 보유조각 전체 조회")
    @GetMapping("/mypage/owned/list")
    public BaseResponseEntity<List<GetOwnedPieceResponseVo>> getOwnedAllPiece(
            @RequestHeader("X-Member-Uuid") String memberUuid
    ) {
        List<GetOwnedPieceResponseVo> result = tradeService.getOwnedPieceByMemberUuid(memberUuid)
                .stream().map(GetOwnedPieceResponseDto::toVo).toList();
        return new BaseResponseEntity<>(result);
    }

    @Operation(summary = "멤버의 보유 조각상품 UUID 리스트 조회")
    @GetMapping("/mypage/owned/uuidlist")
    public BaseResponseEntity<List<GetAllPieceProductUuidResponseVo>> getOwnedPieceUuidList(
            @RequestHeader("X-Member-Uuid") String memberUuid
    ) {
        List<GetAllPieceProductUuidResponseVo> result = tradeService.getOwnedPieceProductUuidByMemberUuid(memberUuid)
                .stream().map(GetAllPieceProductUuidResponseDto::toVo).toList();

        return new BaseResponseEntity<>(result);
    }

    @Operation(summary = "멤버의 조각상품별 보유 조각 개수 조회")
    @GetMapping("/mypage/owned/{pieceProductUuid}")
    public BaseResponseEntity<GetOwnedPieceResponseVo> getPieceCount(
            @RequestHeader("X-Member-Uuid") String memberUuid,
            @PathVariable String pieceProductUuid
    ) {
        GetOwnedPieceResponseVo result = tradeService.getOwnedPieceByMemberAndProductUuid(memberUuid, pieceProductUuid).toVo();
        return new BaseResponseEntity<>(result);
    }

    @Operation(summary = "조각 매수 예약 생성")
    @PostMapping("/buy")
    public BaseResponseEntity<Void> createBuyReservation(
            @RequestHeader(value = "X-Member-Uuid") String memberUuid,
            @RequestBody CreateTradeRequestVo createTradeRequestVo
    ) {
        CreateTradeRequestDto result = CreateTradeRequestDto.fromBuy(memberUuid, createTradeRequestVo);
        tradeService.createBuyReservation(memberUuid, result);
        return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
    }

    @Operation(summary = "조각 매도 예약 생성")
    @PostMapping("/sell")
    public BaseResponseEntity<Void> createSellReservation(
            @RequestHeader(value = "X-Member-Uuid") String memberUuid,
            @RequestBody CreateTradeRequestVo createTradeRequestVo
    ) {
        CreateTradeRequestDto result = CreateTradeRequestDto.fromSell(memberUuid, createTradeRequestVo);
        tradeService.createSellReservation(memberUuid, result);
        return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
    }

}
