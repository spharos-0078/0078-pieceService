package com.pieceofcake.piece_service.piece.application;

import com.pieceofcake.piece_service.piece.dto.in.CreatePieceRequestDto;

public interface PieceService {
    void createPiece(CreatePieceRequestDto createPieceRequestDto);
}
