package com.pieceofcake.piece_service.trade.presentation;

import com.pieceofcake.piece_service.common.entity.BaseResponseEntity;
import com.pieceofcake.piece_service.common.entity.BaseResponseStatus;
import com.pieceofcake.piece_service.trade.application.TradeService;
import com.pieceofcake.piece_service.trade.dto.in.CreateTradeRequestDto;
import com.pieceofcake.piece_service.trade.dto.out.*;
import com.pieceofcake.piece_service.trade.entity.PieceTradeReservation;
import com.pieceofcake.piece_service.trade.entity.QPieceTradeReservation;
import com.pieceofcake.piece_service.trade.vo.in.CreateTradeRequestVo;
import com.pieceofcake.piece_service.trade.vo.out.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("api/v1/piece")
@RequiredArgsConstructor
@RestController
public class TradeController {

    private final TradeService tradeService;

    @Operation(summary = "상품별 소유자, 보유조각 전체 조회 (관리자용)")
    @GetMapping("/owned/{pieceProductUuid}/list")
    public BaseResponseEntity<List<GetOwnedMemberAndPieceQuantityResponseVo>> getOwnedMemberAndQuantity(
            @PathVariable String pieceProductUuid
    ) {
        return new BaseResponseEntity<>(tradeService.getOwnedMemberAndQuantity(pieceProductUuid)
                .stream().map(GetOwnedMemberAndPieceQuantityResponseDto::toVo).toList());
    }

    @Operation(summary = "본인의 보유조각 전체 조회")
    @GetMapping("/mypage/owned/list")
    public BaseResponseEntity<List<GetOwnedPieceResponseVo>> getOwnedAllPiece(
            @RequestHeader("X-Member-Uuid") String memberUuid
    ) {
        List<GetOwnedPieceResponseVo> result = tradeService.getOwnedPieceByMemberUuid(memberUuid)
                .stream().map(GetOwnedPieceResponseDto::toVo).toList();
        return new BaseResponseEntity<>(result);
    }

    @Operation(summary = "본인의 보유 조각상품 UUID 리스트 조회")
    @GetMapping("/mypage/owned/uuidlist")
    public BaseResponseEntity<List<GetAllPieceProductUuidResponseVo>> getOwnedPieceUuidList(
            @RequestHeader("X-Member-Uuid") String memberUuid
    ) {
        List<GetAllPieceProductUuidResponseVo> result = tradeService.getOwnedPieceProductUuidByMemberUuid(memberUuid)
                .stream().map(GetAllPieceProductUuidResponseDto::toVo).toList();

        return new BaseResponseEntity<>(result);
    }

    @Operation(summary = "본인이 보유한 조각상품별 평균단가 정보 상세 조회")
    @GetMapping("/mypage/owned/piece-average/{pieceProductUuid}")
    public BaseResponseEntity<GetPieceAverageResponseVo> getPieceAverage(
            @RequestHeader("X-Member-Uuid") String memberUuid,
            @PathVariable String pieceProductUuid
    ) {
        GetPieceAverageResponseDto pieceAverage = tradeService
                .getPieceAverageByMemberAndPieceProductUuid(memberUuid, pieceProductUuid);
        return new BaseResponseEntity<>(pieceAverage.toVo());
    }

    @Operation(summary = "본인이 보유한 조각상품별 조각 개수 조회")
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

    // 조각 예약 취소
    @Operation(summary = "조각 거래 예약 취소")
    @PutMapping("/cancel/{reservationUuid}")
    public BaseResponseEntity<Void> cancelReservation(
            @RequestHeader("X-Member-Uuid") String memberUuid,
            @PathVariable String reservationUuid
    ) {
        tradeService.cancelReservation(memberUuid, reservationUuid);
        return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
    }

    // 사용자 예약 내역 UUID 리스트 조회
    @Operation(summary = "본인 조각 거래 예약 내역 UUID 리스트 조회")
    @GetMapping("/reservation/list")
    public BaseResponseEntity<List<GetTradeReservationUuidResponseVo>> getReservationUuidList(
            @RequestHeader("X-Member-Uuid") String memberUuid
    ) {
        List<GetTradeReservationUuidResponseVo> uuidList = tradeService.getReservationUuidByMemberUuid(memberUuid)
                .stream().map(GetTradeReservationUuidResponseDto::toVo).toList();
        return new BaseResponseEntity<>(uuidList);
    }

    // 사용자 예약 내역 단건 조회
    @Operation(summary = "본인 조각 거래 예약 내역 상세 조회")
    @GetMapping("/reservation/{reservationUuid}")
    public BaseResponseEntity<GetTradeReservationResponseVo> getReservationDetail(
            @RequestHeader("X-Member-Uuid") String memberUuid,
            @PathVariable String reservationUuid
    ) {
        GetTradeReservationResponseDto getTradeReservationResponseDto = tradeService.getReservationByUuid(memberUuid, reservationUuid);
        return new BaseResponseEntity<>(getTradeReservationResponseDto.toVo());
    }

}