package com.pieceofcake.piece_service.trade.infrastructure;

import com.pieceofcake.piece_service.trade.entity.OwnedPieceAverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OwnedPieceAverageRepository extends JpaRepository<OwnedPieceAverage, Long> {
    Optional<OwnedPieceAverage> findByMemberUuidAndPieceProductUuid(String memberUuid, String pieceProductUuid);
}
