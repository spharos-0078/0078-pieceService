package com.pieceofcake.piece_service.piece.application;

import com.pieceofcake.piece_service.piece.dto.in.ApplyPieceRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.CancelPieceRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.CreatePieceRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.DistributePieceRequestDto;

public interface PieceService {
    void createPiece(CreatePieceRequestDto createPieceRequestDto);

    void deleteAllPieces(String productUuid);

    void distributePiece(DistributePieceRequestDto distributePieceRequestDto);

    void applyForPieces(ApplyPieceRequestDto applyPieceRequestDto);

    void cancelPieces(CancelPieceRequestDto cancelPieceRequestDto);
}
