package com.pieceofcake.piece_service.piece.application;

import com.pieceofcake.piece_service.common.entity.BaseResponseStatus;
import com.pieceofcake.piece_service.common.exception.BaseException;
import com.pieceofcake.piece_service.piece.dto.in.CreatePieceProductRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.GetPieceProductUuidListRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.UpdatePieceProductRequestDto;
import com.pieceofcake.piece_service.piece.dto.out.GetPieceProductUuidListResponseDto;
import com.pieceofcake.piece_service.piece.entity.PieceProduct;
import com.pieceofcake.piece_service.piece.infrastructure.PieceProductCustomImplRepository;
import com.pieceofcake.piece_service.piece.infrastructure.PieceProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PieceProductServiceImpl implements PieceProductService {
    private final PieceProductRepository pieceProductRepository;
    private final PieceProductCustomImplRepository pieceProductCustomRepository;

    @Transactional
    @Override
    public void createPieceProduct(CreatePieceProductRequestDto createPieceProductRequestDto) {
        pieceProductRepository.save(createPieceProductRequestDto.toEntity());
    }

    @Transactional
    @Override
    public void updatePieceProduct(UpdatePieceProductRequestDto updatePieceProductRequestDto) {
        PieceProduct pieceProduct = pieceProductRepository.findByPieceProductUuid(updatePieceProductRequestDto.getProductUuid()).orElseThrow(
                () -> new BaseException(BaseResponseStatus.NO_EXIST_PIECE_PRODUCT)
        );

        pieceProductRepository.save(updatePieceProductRequestDto.toEntity(pieceProduct));
    }

    @Transactional
    @Override
    public void deletePieceProduct(String pieceProductUuid) {
        pieceProductRepository.softDeleteByPieceProductUuid(pieceProductUuid);
    }

    @Override
    public GetPieceProductUuidListResponseDto getPieceProductUuidList(GetPieceProductUuidListRequestDto getPieceProductUuidListRequestDto) {
        return GetPieceProductUuidListResponseDto.from(pieceProductCustomRepository.findProductUuidsByTradingStatus(
                getPieceProductUuidListRequestDto.getPageable(),
                getPieceProductUuidListRequestDto.getIsTrading()
        ));
    }
}
