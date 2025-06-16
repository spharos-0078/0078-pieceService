package com.pieceofcake.piece_service.trade.application;

import com.pieceofcake.piece_service.trade.entity.PieceTradeReservation;

public interface MatchingService {

    void match(PieceTradeReservation reservation);

}
