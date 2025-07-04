package com.pieceofcake.piece_service.piece.presentation;

import com.pieceofcake.piece_service.common.entity.BaseResponseEntity;
import com.pieceofcake.piece_service.common.entity.BaseResponseStatus;
import com.pieceofcake.piece_service.piece.application.PieceServiceImpl;
import com.pieceofcake.piece_service.piece.dto.in.ApplyPieceRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.CancelPieceRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.CreatePieceRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.DistributePieceRequestDto;
import com.pieceofcake.piece_service.piece.vo.in.ApplyPieceRequestVo;
import com.pieceofcake.piece_service.piece.vo.in.CancelPieceRequestVo;
import com.pieceofcake.piece_service.piece.vo.in.CreatePieceRequestVo;
import com.pieceofcake.piece_service.piece.vo.in.DistributePieceRequestVo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/piece")
@RequiredArgsConstructor
@RestController
public class PieceController {

    private final PieceServiceImpl pieceService;

    @Operation(summary = "조각 생성")
    @PostMapping
    public BaseResponseEntity<Void> createPieces(@RequestBody CreatePieceRequestVo createPieceRequestVo) {
        pieceService.createPiece(CreatePieceRequestDto.from(createPieceRequestVo));
        return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
    }

    @Operation(summary = "조각 분배 신청(삭제 예정)")
    @PutMapping("/distribute")
    public BaseResponseEntity<Void> distributePiece(
            @RequestHeader(value = "X-Member-Uuid") String memberUuid,
            @RequestBody DistributePieceRequestVo distributePieceRequestVo
    ) {
        pieceService.distributePiece(DistributePieceRequestDto.of(memberUuid, distributePieceRequestVo));
        return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
    }

    @Operation(summary = "조각 분배 신청")
    @PutMapping("/apply")
    public BaseResponseEntity<Void> applyPiece(
            @RequestHeader(value = "X-Member-Uuid") String memberUuid,
            @RequestBody ApplyPieceRequestVo applyPieceRequestVo
    ) {
        pieceService.applyForPieces(ApplyPieceRequestDto.of(memberUuid, applyPieceRequestVo));
        return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
    }

    @Operation(summary = "조각 분배 신청 취소")
    @PutMapping("/cancel")
    public BaseResponseEntity<Void> cancelPiece(
            @RequestHeader(value = "X-Member-Uuid") String memberUuid,
            @RequestBody CancelPieceRequestVo cancelPieceRequestVo
    ) {
        pieceService.cancelPieces(CancelPieceRequestDto.of(memberUuid, cancelPieceRequestVo));
        return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
    }

    @Operation(summary = "조각 전체 삭제")
    @DeleteMapping("/delete-all/{productUuid}")
    public BaseResponseEntity<Void> deleteAllPieces(@PathVariable String productUuid) {
        pieceService.deleteAllPieces(productUuid);
        return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
    }
}
