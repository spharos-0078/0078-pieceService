package com.pieceofcake.piece_service.piece.application;

import com.pieceofcake.piece_service.piece.dto.in.CreatePieceProductRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.GetPieceProductUuidListRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.UpdatePieceProductRequestDto;
import com.pieceofcake.piece_service.piece.dto.out.GetPieceProductUuidListResponseDto;

public interface PieceProductService {
    void createPieceProduct(CreatePieceProductRequestDto createPieceProductRequestDto);
    void updatePieceProduct(UpdatePieceProductRequestDto updatePieceProductRequestDto);
    void deletePieceProduct(String pieceProductUuid);
    // uuid list 조회(paging 필요)
    GetPieceProductUuidListResponseDto getPieceProductUuidList(GetPieceProductUuidListRequestDto getPieceProductUuidListRequestDto);
}
