package com.pieceofcake.piece_service.piece.infrastructure;

import com.pieceofcake.piece_service.piece.entity.LikedPieceProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikedPieceProductRepository extends JpaRepository<LikedPieceProduct, Long> {
    Optional<LikedPieceProduct> findByPieceProductUuidAndMemberUuid(String pieceProductUuid, String memberUuid);
    List<LikedPieceProduct> getLikedPieceProductByMemberUuid(String memberUuid);
    Boolean existsByPieceProductUuidAndMemberUuid(String pieceProductUuid, String memberUuid);
}
