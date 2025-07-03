package com.pieceofcake.piece_service.piece.application;

import com.pieceofcake.piece_service.piece.dto.in.CreatePieceProductLikeRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.CreatePieceProductRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.GetPieceProductUuidListRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.UpdatePieceProductRequestDto;
import com.pieceofcake.piece_service.piece.dto.out.GetLikedPieceProductResponseDto;
import com.pieceofcake.piece_service.piece.dto.out.GetMarketPriceResponseDto;
import com.pieceofcake.piece_service.piece.dto.out.GetPieceProductUuidListResponseDto;
import com.pieceofcake.piece_service.piece.entity.PieceProduct;
import com.pieceofcake.piece_service.piece.vo.out.GetMarketPriceResponseVo;

import java.util.List;

public interface PieceProductService {
    PieceProduct createPieceProduct(CreatePieceProductRequestDto createPieceProductRequestDto);
    void updatePieceProduct(UpdatePieceProductRequestDto updatePieceProductRequestDto);
    void deletePieceProduct(String pieceProductUuid);
    // uuid list 조회(paging 필요)
    GetPieceProductUuidListResponseDto getPieceProductUuidList(GetPieceProductUuidListRequestDto getPieceProductUuidListRequestDto);
    GetMarketPriceResponseDto getMarketPrice(String pieceProductUuid);
    void likePieceProduct(CreatePieceProductLikeRequestDto createPieceProductLikeRequestDto);
    List<GetLikedPieceProductResponseDto> getLikedPieceProductList(String memberUuid);
    Boolean isLikedPieceProduct(String pieceProductUuid, String memberUuid);
}