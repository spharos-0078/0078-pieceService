package com.pieceofcake.piece_service.kafka.application;

import com.pieceofcake.piece_service.kafka.dto.FundingCompleteKafkaDto;
import com.pieceofcake.piece_service.piece.entity.Piece;
import com.pieceofcake.piece_service.piece.entity.PieceProduct;
import com.pieceofcake.piece_service.piece.infrastructure.PieceProductRepository;
import com.pieceofcake.piece_service.piece.infrastructure.PieceRepository;
import com.pieceofcake.piece_service.trade.entity.OwnedPiece;
import com.pieceofcake.piece_service.trade.entity.OwnedPieceAverage;
import com.pieceofcake.piece_service.trade.infrastructure.OwnedPieceAverageRepository;
import com.pieceofcake.piece_service.trade.infrastructure.OwnedPieceRepository;
import com.pieceofcake.piece_service.trade.infrastructure.feign.client.BoardFeignClient;
import com.pieceofcake.piece_service.trade.infrastructure.feign.dto.CreateBoardRequestFeignDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class FundingCompleteService {

    private final PieceRepository pieceRepository;
    private final OwnedPieceRepository ownedPieceRepository;
    private final PieceProductRepository pieceProductRepository;
    private final OwnedPieceAverageRepository ownedPieceAverageRepository;
    private final BoardFeignClient boardFeignClient;

    @Transactional
    public void processFundingComplete(FundingCompleteKafkaDto dto) {
        // 1. 조각상품 생성
        PieceProduct pieceProduct = PieceProduct.builder()
                .pieceProductUuid(UUID.randomUUID().toString())
                .productUuid(dto.getProductUuid())
                .marketPrice(dto.getPiecePrice())
                .totalPieces(dto.getTotalPieces())
                .isTrading(dto.isTrading())
                .build();
        pieceProductRepository.save(pieceProduct);

        // 조각상품 게시판 생성
        CreateBoardRequestFeignDto boardRequest = CreateBoardRequestFeignDto.builder()
                .boardType("PIECE")
                .boardUuid(pieceProduct.getPieceProductUuid())
                .build();

        try {
            boardFeignClient.createCommunityBoard(boardRequest);
            log.info("[📢] 게시판 생성 완료 - pieceProductUuid: {}", pieceProduct.getPieceProductUuid());
        } catch (Exception e) {
            log.error("[❌] 게시판 생성 실패", e);
        }

        // 2. 조각 정보 조회
        List<Piece> pieceList = pieceRepository.findByProductUuid(dto.getProductUuid());
        if (pieceList.size() != dto.getTotalPieces()) {
            throw new IllegalStateException("조각 수량 불일치: 요청 " + dto.getTotalPieces() + ", 실제 " + pieceList.size());
        }

        long unownedCount = pieceList.stream().filter(p -> p.getMemberUuid() == null).count();
        if (unownedCount > 0) {
            log.warn("[⚠️] memberUuid가 없는 조각이 {}개 존재합니다. 해당 조각은 보유조각 처리에서 제외됩니다.", unownedCount);
        }

        // 3. memberUuid → 조각 리스트로 그룹화
        Map<String, List<Piece>> pieceMapByMember = pieceList.stream()
                .filter(p -> p.getMemberUuid() != null)
                .collect(Collectors.groupingBy(Piece::getMemberUuid));

        for (Map.Entry<String, List<Piece>> entry : pieceMapByMember.entrySet()) {
            String memberUuid = entry.getKey();
            List<Piece> pieces = entry.getValue();

            // 3-1. OwnedPiece 저장
            List<OwnedPiece> ownedPieces = pieces.stream()
                    .map(p -> OwnedPiece.builder()
                            .ownedPieceUuid(UUID.randomUUID().toString())
                            .memberUuid(memberUuid)
                            .pieceProductUuid(pieceProduct.getPieceProductUuid())
                            .pieceUuid(p.getPieceUuid())
                            .tradePrice(dto.getPiecePrice())
                            .build())
                    .toList();
            ownedPieceRepository.saveAll(ownedPieces);

            // 3-2. OwnedPieceAverage 갱신
            Optional<OwnedPieceAverage> optionalAverage =
                    ownedPieceAverageRepository.findByMemberUuidAndPieceProductUuid(memberUuid, pieceProduct.getPieceProductUuid());

            int quantity = pieces.size();
            long totalAmount = dto.getPiecePrice() * quantity;

            if (optionalAverage.isPresent()) {
                OwnedPieceAverage average = optionalAverage.get();
                long newTotalAmount = average.getTotalAmount() + totalAmount;
                int newTotalQuantity = average.getTotalQuantity() + quantity;
                long newAveragePrice = newTotalAmount / newTotalQuantity;

                average.update(newTotalAmount, newTotalQuantity, newAveragePrice);
            } else {
                OwnedPieceAverage newAverage = OwnedPieceAverage.builder()
                        .pieceProductUuid(pieceProduct.getPieceProductUuid())
                        .memberUuid(memberUuid)
                        .totalAmount(totalAmount)
                        .totalQuantity(quantity)
                        .averagePrice(dto.getPiecePrice())
                        .build();
                ownedPieceAverageRepository.save(newAverage);
            }
        }

        log.info("[✅] 공모 완료 처리 완료: 조각상품 생성 + 보유조각 저장 + 평균단가 갱신");
    }

}
