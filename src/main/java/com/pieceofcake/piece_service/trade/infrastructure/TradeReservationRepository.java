package com.pieceofcake.piece_service.trade.infrastructure;

import com.pieceofcake.piece_service.trade.entity.PieceTradeReservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TradeReservationRepository extends JpaRepository<PieceTradeReservation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM PieceTradeReservation t " +
            "WHERE t.pieceProductUuid = :pieceProductUuid " +
            "AND t.tradeType = 'SELL' " +
            "AND t.registeredPrice <= :price " +
            "AND t.tradeStatus = 'WAITING' " +
            "ORDER BY t.registeredPrice ASC, t.id ASC")
    List<PieceTradeReservation> findMatchingSellReservations(
            @Param("pieceProductUuid") String pieceProductUuid,
            @Param("price") Long price
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM PieceTradeReservation t " +
            "WHERE t.pieceProductUuid = :pieceProductUuid " +
            "AND t.tradeType = 'BUY' " +
            "AND t.registeredPrice >= :price " +
            "AND t.tradeStatus = 'WAITING' " +
            "ORDER BY t.registeredPrice DESC, t.id ASC")
    List<PieceTradeReservation> findMatchingBuyReservations(
            @Param("pieceProductUuid") String pieceProductUuid,
            @Param("price") Long price
    );

}
