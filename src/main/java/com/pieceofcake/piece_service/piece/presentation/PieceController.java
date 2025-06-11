package com.pieceofcake.piece_service.piece.presentation;

import com.pieceofcake.piece_service.common.entity.BaseResponseEntity;
import com.pieceofcake.piece_service.common.entity.BaseResponseStatus;
import com.pieceofcake.piece_service.piece.application.PieceServiceImpl;
import com.pieceofcake.piece_service.piece.dto.in.CreatePieceRequestDto;
import com.pieceofcake.piece_service.piece.vo.in.CreatePieceRequestVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("api/v1/piece")
@RequiredArgsConstructor
@RestController
public class PieceController {

    private final PieceServiceImpl pieceService;

    @PostMapping
    public BaseResponseEntity<Void> createPieces(@RequestBody CreatePieceRequestVo createPieceRequestVo) {
        pieceService.createPiece(CreatePieceRequestDto.from(createPieceRequestVo));
        return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
    }
}
