package com.pieceofcake.piece_service.trade.presentation;

import com.pieceofcake.piece_service.common.entity.BaseResponseEntity;
import com.pieceofcake.piece_service.trade.application.MatchedHistoryServiceImpl;
import com.pieceofcake.piece_service.trade.dto.in.GetMatchedHistoryListRequestDto;
import com.pieceofcake.piece_service.trade.vo.out.GetMatchedHistoryListPageResponseVo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/piece")
@RequiredArgsConstructor
@RestController
public class MatchedHistoryController {

    private final MatchedHistoryServiceImpl tradeHistoryService;

    @Operation(summary = "조각 상품별, 본인의 조각 채결내역 조회")
    @GetMapping("/history")
    public BaseResponseEntity<GetMatchedHistoryListPageResponseVo> getTradeHistoryList(
            @RequestHeader("X-Member-Uuid") String memberUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "pieceProductUuid", required = true) String pieceProductUuid
    ){
        return new BaseResponseEntity<>(
                tradeHistoryService.getTradeHistoryList(
                        GetMatchedHistoryListRequestDto.of(page, size, pieceProductUuid, memberUuid)
                ).toVo()
        );
    }

}
