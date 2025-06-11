package com.pieceofcake.piece_service.trade.infrastructure;

import com.pieceofcake.piece_service.trade.entity.PieceTradeReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeReservationRepository extends JpaRepository<PieceTradeReservation, Long> {
}
