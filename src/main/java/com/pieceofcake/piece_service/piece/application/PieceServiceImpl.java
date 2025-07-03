package com.pieceofcake.piece_service.piece.application;

import com.pieceofcake.piece_service.common.entity.BaseResponseStatus;
import com.pieceofcake.piece_service.common.exception.BaseException;
import com.pieceofcake.piece_service.piece.dto.in.ApplyPieceRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.CancelPieceRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.CreatePieceRequestDto;
import com.pieceofcake.piece_service.piece.dto.in.DistributePieceRequestDto;
import com.pieceofcake.piece_service.piece.entity.Piece;
import com.pieceofcake.piece_service.piece.infrastructure.PieceJdbcRepository;
import com.pieceofcake.piece_service.piece.infrastructure.PieceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Service
public class PieceServiceImpl implements PieceService {

    private final PieceJdbcRepository pieceJdbcRepository;
    private final PieceRepository pieceRepository;

    @Override
    @Transactional
    public void createPiece(CreatePieceRequestDto createPieceRequestDto) {
        List<Piece> pieces = IntStream.rangeClosed(1, createPieceRequestDto.getTotalPieces())
                .mapToObj(i -> Piece.builder()
                        .pieceUuid(UUID.randomUUID().toString())
                        .serialNumber(i)
                        .productUuid(createPieceRequestDto.getProductUuid())
                        .build())
                .toList();

        pieceJdbcRepository.savePieces(pieces);
    }

    @Transactional
    @Override
    public void distributePiece(DistributePieceRequestDto distributePieceRequestDto) {
        if (distributePieceRequestDto.getApplyStatus()) {
            List<Piece> availablePieces = pieceRepository.findTopNByProductUuidAndMemberUuidIsNull(
                    distributePieceRequestDto.getProductUuid(), Pageable.ofSize(distributePieceRequestDto.getPieceQuantity())
            );

            if (availablePieces.size() < distributePieceRequestDto.getPieceQuantity()) {
                throw new IllegalStateException("요청한 수량만큼의 조각이 존재하지 않습니다.");
            }

            availablePieces.forEach(piece -> piece.changeAssignStatusToMember(distributePieceRequestDto.getMemberUuid()));
        } else {
            List<Piece> ownedPieces = pieceRepository.findByProductUuidAndMemberUuid(
                    distributePieceRequestDto.getProductUuid(), distributePieceRequestDto.getMemberUuid()
            );

            ownedPieces.forEach(piece -> piece.changeAssignStatusToMember(null));
        }
    }

    @Transactional
    @Override
    public void applyForPieces(ApplyPieceRequestDto requestDto) {

        long availableCount = pieceRepository.countByProductUuidAndMemberUuidIsNull(
                requestDto.getProductUuid());

        if (availableCount < requestDto.getPieceQuantity()) {
            throw new BaseException(BaseResponseStatus.NO_ENOUGH_EXIST_PIECE);
        }

        // 가용 조각 N개를 락을 걸고 한 번에 조회
        List<Piece> availablePieces = pieceRepository.findByProductUuidAndMemberUuidIsNullOrderByIdAsc(
                requestDto.getProductUuid(),  PageRequest. of(0, requestDto.getPieceQuantity()));

        if (availablePieces.size() < requestDto.getPieceQuantity()) {
            throw new BaseException(BaseResponseStatus.NO_ENOUGH_EXIST_PIECE);
        }

        pieceJdbcRepository.updateMemberInBatch(
                availablePieces.stream()
                        .map(Piece::getId)
                        .collect(Collectors.toList()),
                requestDto.getMemberUuid());
    }

    @Transactional
    @Override
    public void cancelPieces(CancelPieceRequestDto requestDto) {
        // 현재 사용자에게 할당된 조각 조회
        List<Piece> ownedPieces = pieceRepository.findByProductUuidAndMemberUuid(
                requestDto.getProductUuid(), requestDto.getMemberUuid());

        pieceJdbcRepository.updateMemberInBatch(
                ownedPieces.stream()
                        .map(Piece::getId)
                        .collect(Collectors.toList()),
                null); // null로 member_uuid 초기화 → 취소
    }

    @Transactional
    @Override
    public void deleteAllPieces(String productUuid) {
        pieceJdbcRepository.deletePiecesByProductUuid(productUuid);
    }
}
