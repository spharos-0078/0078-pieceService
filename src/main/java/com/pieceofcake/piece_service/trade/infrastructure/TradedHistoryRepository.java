package com.pieceofcake.piece_service.trade.infrastructure;

import com.pieceofcake.piece_service.trade.entity.PieceTradedHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradedHistoryRepository extends JpaRepository<PieceTradedHistory, Long> {
}
