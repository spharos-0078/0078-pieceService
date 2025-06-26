package com.pieceofcake.piece_service.trade.infrastructure;

import com.pieceofcake.piece_service.trade.entity.PieceTradeReservation;
import com.pieceofcake.piece_service.trade.entity.TradeStatus;
import com.pieceofcake.piece_service.trade.entity.TradeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TradeReservationRepository extends JpaRepository<PieceTradeReservation, Long> {

    @Query("SELECT r FROM PieceTradeReservation r " +
            "WHERE r.pieceProductUuid = :uuid " +
            "AND r.tradeType = :type " +
            "AND r.tradeStatus = 'WAITING'")
    List<PieceTradeReservation> findWaitingByProductAndType(
            @Param("uuid") String pieceProductUuid,
            @Param("type") TradeType tradeType,
            @Param("status") TradeStatus tradeStatus
    );

    List<PieceTradeReservation> findByTradeStatus(TradeStatus tradeStatus);

}
