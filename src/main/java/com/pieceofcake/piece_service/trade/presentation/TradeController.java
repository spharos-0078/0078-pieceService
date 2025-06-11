package com.pieceofcake.piece_service.trade.presentation;

import com.pieceofcake.piece_service.common.entity.BaseResponseEntity;
import com.pieceofcake.piece_service.trade.application.TradeService;
import com.pieceofcake.piece_service.trade.dto.out.GetOwnedPieceResponseDto;
import com.pieceofcake.piece_service.trade.vo.out.GetOwnedPieceResponseVo;
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
    public BaseResponseEntity<List<GetOwnedPieceResponseVo>> getOwnedPiece(
            @RequestHeader("X-Member-Uuid") String memberUuid
    ) {
        List<GetOwnedPieceResponseVo> result = tradeService.getOwnedPieceByMemberUuid(memberUuid)
                .stream().map(GetOwnedPieceResponseDto::toVo).toList();
        return new BaseResponseEntity<>(result);
    }

}
