package com.pieceofcake.piece_service.piece.application;

import com.pieceofcake.piece_service.piece.dto.in.CreatePieceRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.DistributePieceRequestDto;

public interface PieceService {
    void createPiece(CreatePieceRequestDto createPieceRequestDto);

    void distributePiece(DistributePieceRequestDto distributePieceRequestDto);
}
