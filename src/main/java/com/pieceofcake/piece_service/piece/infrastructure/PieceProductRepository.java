package com.pieceofcake.piece_service.piece.infrastructure;

import com.pieceofcake.piece_service.piece.entity.PieceProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PieceProductRepository extends JpaRepository<PieceProduct, Long> {
    Optional<PieceProduct> findByPieceProductUuid(String pieceProductUuid);

    @Modifying
    @Query("UPDATE PieceProduct p SET p.isDeleted = true WHERE p.pieceProductUuid = :pieceProductUuid")
    void softDeleteByPieceProductUuid(@Param("pieceProductUuid") String pieceProductUuid);

    @Modifying
    @Query("UPDATE PieceProduct p SET p.marketPrice = :price WHERE p.pieceProductUuid = :pieceProductUuid")
    void updateMarketPrice(@Param("pieceProductUuid") String pieceProductUuid, @Param("price") long price);
}
