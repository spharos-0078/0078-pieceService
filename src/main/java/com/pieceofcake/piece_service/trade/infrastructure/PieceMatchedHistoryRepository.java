package com.pieceofcake.piece_service.trade.infrastructure;

import com.pieceofcake.piece_service.trade.entity.PieceMatchedHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PieceMatchedHistoryRepository extends JpaRepository<PieceMatchedHistory, Long> {
    Page<PieceMatchedHistory> findAllByMemberUuidAndPieceProductUuid(
            String memberUuid,
            String pieceProductUuid,
            Pageable pageable
    );
}
