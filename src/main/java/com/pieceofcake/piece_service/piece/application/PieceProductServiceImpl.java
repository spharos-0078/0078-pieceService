package com.pieceofcake.piece_service.piece.application;

import com.pieceofcake.piece_service.common.entity.BaseResponseStatus;
import com.pieceofcake.piece_service.common.exception.BaseException;
import com.pieceofcake.piece_service.kafka.event.PieceEvent;
import com.pieceofcake.piece_service.kafka.producer.PieceKafkaProducer;
import com.pieceofcake.piece_service.piece.dto.in.CreatePieceProductRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.GetPieceProductUuidListRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.UpdatePieceProductRequestDto;
import com.pieceofcake.piece_service.piece.dto.out.GetPieceProductUuidListResponseDto;
import com.pieceofcake.piece_service.piece.entity.PieceProduct;
import com.pieceofcake.piece_service.piece.entity.PieceStatus;
import com.pieceofcake.piece_service.piece.infrastructure.PieceProductCustomImplRepository;
import com.pieceofcake.piece_service.piece.infrastructure.PieceProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PieceProductServiceImpl implements PieceProductService {
    private final PieceProductRepository pieceProductRepository;
    private final PieceProductCustomImplRepository pieceProductCustomRepository;
    private final PieceKafkaProducer pieceKafkaProducer;

    @Transactional
    @Override
    public void createPieceProduct(CreatePieceProductRequestDto createPieceProductRequestDto) {
        PieceProduct pieceProduct = pieceProductRepository.save(createPieceProductRequestDto
                .toEntity(UUID.randomUUID().toString()));

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                PieceEvent event = PieceEvent.builder()
                        .productUuid(pieceProduct.getProductUuid())
                        .pieceProductUuid(pieceProduct.getPieceProductUuid())
                        .isTrading(pieceProduct.getIsTrading())
                        .status(PieceStatus.NONE)
                        .build();

                pieceKafkaProducer.sendCreatePieceEvent(event);
            }
        });
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
