package com.pieceofcake.piece_service.trade.application.domain;

import com.pieceofcake.piece_service.trade.entity.OwnedPiece;
import com.pieceofcake.piece_service.trade.entity.PieceTradeReservation;
import com.pieceofcake.piece_service.trade.infrastructure.OwnedPieceRepository;
import com.pieceofcake.piece_service.trade.infrastructure.TradeReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompensationService {
    
    private final OwnedPieceRepository ownedPieceRepository;
    private final TradeReservationRepository tradeReservationRepository;
    
    /**
     * 소유권 이전 실패 시 보상 처리
     */
    @Transactional
    public void compensateOwnershipTransfer(List<OwnedPiece> pieces, String originalOwnerUuid) {
        try {
            for (OwnedPiece piece : pieces) {
                // 원래 소유자에게 조각 반환
                OwnedPiece restoredPiece = OwnedPiece.builder()
                        .pieceUuid(piece.getPieceUuid())
                        .memberUuid(originalOwnerUuid)
                        .pieceProductUuid(piece.getPieceProductUuid())
                        .build();
                
                ownedPieceRepository.save(restoredPiece);
                log.info("소유권 이전 보상 완료: pieceUuid={}, originalOwner={}", 
                        piece.getPieceUuid(), originalOwnerUuid);
            }
        } catch (Exception e) {
            log.error("소유권 이전 보상 실패: originalOwner={}", originalOwnerUuid, e);
            throw e;
        }
    }
    
    /**
     * 예약 상태 복원
     */
    @Transactional
    public void compensateReservationStatus(PieceTradeReservation reservation, int originalQuantity) {
        try {
            reservation.restoreQuantity(originalQuantity);
            tradeReservationRepository.save(reservation);
            log.info("예약 상태 복원 완료: reservationUuid={}, quantity={}", 
                    reservation.getReservationUuid(), originalQuantity);
        } catch (Exception e) {
            log.error("예약 상태 복원 실패: reservationUuid={}", 
                    reservation.getReservationUuid(), e);
            throw e;
        }
    }
} 