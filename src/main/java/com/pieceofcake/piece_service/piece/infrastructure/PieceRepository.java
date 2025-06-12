package com.pieceofcake.piece_service.piece.infrastructure;

import com.pieceofcake.piece_service.piece.entity.Piece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PieceRepository extends JpaRepository<Piece, Long> {
    boolean existsByPieceUuid(String pieceUuid);

    @Modifying
    @Query("UPDATE Piece p SET p.memberUuid = :memberUuid WHERE p.pieceUuid = :pieceUuid")
    void updateOwner(@Param("pieceUuid") String pieceUuid, @Param("memberUuid") String memberUuid);

}
