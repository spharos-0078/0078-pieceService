package com.pieceofcake.piece_service.trade.application.domain;

import com.pieceofcake.piece_service.trade.entity.OwnedPiece;
import com.pieceofcake.piece_service.trade.entity.PieceTradeReservation;
import com.pieceofcake.piece_service.trade.entity.TradeType;
import com.pieceofcake.piece_service.trade.infrastructure.OwnedPieceRepository;
import com.pieceofcake.piece_service.trade.infrastructure.PieceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeExecutionService {
    
    private final OwnedPieceRepository ownedPieceRepository;
    private final PieceRepository pieceRepository;
    
    /**
     * 소유권 이전 처리
     * 트랜잭션 경계를 명확히 하기 위해 별도 서비스로 분리
     */
    @Transactional
    public void transferOwnership(List<OwnedPiece> sellerPieces, String newOwnerUuid) {
        for (OwnedPiece piece : sellerPieces) {
            // 새로운 소유자에게 조각 할당
            OwnedPiece newOwnedPiece = OwnedPiece.builder()
                    .pieceUuid(piece.getPieceUuid())
                    .memberUuid(newOwnerUuid)
                    .pieceProductUuid(piece.getPieceProductUuid())
                    .build();
            
            ownedPieceRepository.save(newOwnedPiece);
            ownedPieceRepository.delete(piece);
            pieceRepository.updateOwner(piece.getPieceUuid(), newOwnerUuid);
            
            log.info("소유권 이전 완료: pieceUuid={}, newOwner={}", piece.getPieceUuid(), newOwnerUuid);
        }
    }
    
    /**
     * 매도자 보유 조각 조회 (락을 걸고 조회)
     */
    @Transactional
    public List<OwnedPiece> getSellerPiecesWithLock(String memberUuid, String pieceProductUuid, int quantity) {
        List<OwnedPiece> ownedPieces = ownedPieceRepository
                .findByMemberUuidAndPieceProductUuidForUpdate(memberUuid, pieceProductUuid);
        
        if (ownedPieces.size() < quantity) {
            throw new IllegalStateException("보유 조각이 부족합니다. 요청: " + quantity + ", 보유: " + ownedPieces.size());
        }
        
        return ownedPieces.subList(0, quantity);
    }
} 