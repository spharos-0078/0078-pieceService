package com.pieceofcake.piece_service.trade.infrastructure;

import com.pieceofcake.piece_service.trade.entity.OwnedPiece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface OwnedPieceRepository extends JpaRepository<OwnedPiece, Long> {

    // 멤버/조각상품별 조각 개수 집계
    @Query("SELECT o.pieceProductUuid, COUNT(o) " +
            "FROM OwnedPiece o " +
            "WHERE o.memberUuid = :memberUuid " +
            "GROUP BY o.pieceProductUuid")
    List<Object[]> countOwnedPiecesByMemberUuid(@Param("memberUuid") String memberUuid);

}
