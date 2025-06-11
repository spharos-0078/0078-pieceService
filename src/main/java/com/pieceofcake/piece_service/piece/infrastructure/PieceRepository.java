package com.pieceofcake.piece_service.piece.infrastructure;

import com.pieceofcake.piece_service.piece.entity.Piece;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PieceRepository extends JpaRepository<Piece, Long> {
}
