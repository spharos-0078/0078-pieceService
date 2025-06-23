package com.pieceofcake.piece_service.piece.infrastructure;

import com.pieceofcake.piece_service.piece.entity.Piece;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PieceRepository extends JpaRepository<Piece, Long> {
    boolean existsByPieceUuid(String pieceUuid);

    @Modifying
    @Query("UPDATE Piece p SET p.memberUuid = :memberUuid WHERE p.pieceUuid = :pieceUuid")
    void updateOwner(@Param("pieceUuid") String pieceUuid, @Param("memberUuid") String memberUuid);

    @Query("SELECT p FROM Piece p WHERE p.productUuid = :productUuid AND p.memberUuid IS NULL")
    List<Piece> findTopNByProductUuidAndMemberUuidIsNull(@Param("productUuid") String productUuid, Pageable pageable);

    List<Piece> findByProductUuidAndMemberUuid(String productUuid, String memberUuid);

    void deleteAllByProductUuid(String productUuid);
}
