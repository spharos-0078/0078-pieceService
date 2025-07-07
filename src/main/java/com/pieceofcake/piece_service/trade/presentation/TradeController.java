package com.pieceofcake.piece_service.trade.presentation;

import com.pieceofcake.piece_service.common.entity.BaseResponseEntity;
import com.pieceofcake.piece_service.common.entity.BaseResponseStatus;
import com.pieceofcake.piece_service.trade.application.TradeService;
import com.pieceofcake.piece_service.trade.application.sse.TradeSseEventService;
import com.pieceofcake.piece_service.trade.dto.in.CreateTradeRequestDto;
import com.pieceofcake.piece_service.trade.dto.out.*;
import com.pieceofcake.piece_service.trade.vo.in.CreateTradeRequestVo;
import com.pieceofcake.piece_service.trade.vo.out.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@RequestMapping("/api/v1/piece")
@RequiredArgsConstructor
@RestController
public class TradeController {

    private final TradeService tradeService;
    private final TradeSseEventService tradeSseEventService;

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

    @Operation(
            summary = "조각 거래 호가 조회 SSE API",
            description = "Server-Sent Events를 사용하여 실시간으로 호가 정보 업데이트를 스트리밍하는 API입니다.\n\n" +
                    "- path variable로 조각 상품 UUID를 받아 해당 조각상품의 호가 정보 업데이트 이벤트를 실시간으로 제공합니다.\n" +
                    "- 클라이언트는 이 엔드포인트에 연결하여 가격 변동을 실시간으로 모니터링할 수 있습니다.\n\n" +
                    "- 응답 데이터 스키마:\n\n" +
                    "            - askp: 매도 호가 가격 리스트 (체결가 기준 위로 10단계 가격)\n\n" +
                    "              예) [1010, 1015, 1020, ...]\n\n" +
                    "            - bidp: 매수 호가 가격 리스트 (체결가 기준 아래로 10단계 가격)\n\n" +
                    "              예) [1005, 1000, 995, ...]\n\n" +
                    "            - askpRsqn: 각 매도 호가 가격에 대응하는 매도 잔량 리스트\n\n" +
                    "              예) [0, 8, 0, ...]\n\n" +
                    "            - bidRsqn: 각 매수 호가 가격에 대응하는 매수 잔량 리스트\n\n" +
                    "              예) [0, 0, 0, ...]"
    )
    @GetMapping(value = "/sse/quotes-update/{pieceProductUuid}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<UpdateQuotesSseDto>> streamPieceTradeQuotes(
            @PathVariable("pieceProductUuid") String pieceProductUuid) {

        Flux<UpdateQuotesSseDto> flux = tradeSseEventService.getQuotesUpdatesByPieceProductUuid(pieceProductUuid);

        return flux
                .doOnSubscribe(subscription ->
                        log.info("[SSE] 구독 시작: pieceProductUuid={}", pieceProductUuid))
                .doOnCancel(() ->
                        log.info("[SSE] 구독 취소: pieceProductUuid={}", pieceProductUuid))
                .doOnComplete(() ->
                        log.info("[SSE] 구독 정상 종료: pieceProductUuid={}", pieceProductUuid))
                .doOnError(e ->
                        log.error("[SSE] 구독 중 에러 발생: pieceProductUuid={}", pieceProductUuid, e))
                .map(event -> ServerSentEvent.<UpdateQuotesSseDto>builder()
                        .event("quotes-update")
                        .data(event)
                        .build());
//        return tradeSseEventService.getQuotesUpdatesByPieceProductUuid(pieceProductUuid)
//                .map(event -> ServerSentEvent.<UpdateQuotesSseDto>builder()
//                        .event("quotes-update")
//                        .data(event)
//                        .build());
    }

    @Operation(
            summary = "조각 거래 체결 조회 SSE API",
            description = "Server-Sent Events를 사용하여 실시간으로 현재가(체결가) 업데이트를 스트리밍하는 API입니다.\n\n" +
                    "- path variable로 조각 상품 UUID를 받아 해당 조각상품의 현재가 업데이트 이벤트를 실시간으로 제공합니다.\n" +
                    "- 클라이언트는 이 엔드포인트에 연결하여 가격 변동을 실시간으로 모니터링할 수 있습니다.\n\n" +
                    "- 응답 데이터 스키마:\n\n" +
                    "            - marketPrice: 체결가\n\n" +
                    "              예) 1010"
    )
    @GetMapping(value = "/sse/market-price-update/{pieceProductUuid}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<UpdateMarketPriceSseDto>> streamPieceTradeMarketPrice(
            @PathVariable("pieceProductUuid") String pieceProductUuid) {
        return tradeSseEventService.getMatchedUpdatesByPieceProductUuid(pieceProductUuid)
                .map(event -> ServerSentEvent.<UpdateMarketPriceSseDto>builder()
                        .event("market-price-update")
                        .data(event)
                        .build());
    }

}