package com.pieceofcake.piece_service.trade.infrastructure;

import com.pieceofcake.piece_service.trade.entity.OwnedPiece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
public interface OwnedPieceRepository extends JpaRepository<OwnedPiece, Long> {

    // 멤버/조각상품별 조각 개수 집계
    @Query("SELECT o.pieceProductUuid, COUNT(o) " +
            "FROM OwnedPiece o " +
            "WHERE o.memberUuid = :memberUuid " +
            "GROUP BY o.pieceProductUuid")
    List<Object[]> countOwnedPiecesByMemberUuid(@Param("memberUuid") String memberUuid);


    List<OwnedPiece> findByMemberUuidAndPieceProductUuid(String memberUuid, String pieceProductUuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT op FROM OwnedPiece op WHERE op.memberUuid = :memberUuid AND op.pieceProductUuid = :pieceProductUuid ORDER BY op.id")
    List<OwnedPiece> findByMemberUuidAndPieceProductUuidForUpdate(
            @Param("memberUuid") String memberUuid, 
            @Param("pieceProductUuid") String pieceProductUuid);

    Long countByMemberUuidAndPieceProductUuid(String memberUuid, String pieceProductUuid);

    @Query("SELECT o.memberUuid, COUNT(o) " +
            "FROM OwnedPiece o " +
            "WHERE o.pieceProductUuid = :pieceProductUuid " +
            "GROUP BY o.memberUuid")
    List<Object[]> countOwnedMemberUuidAndQuantityByPieceProductUuid(@Param("pieceProductUuid") String pieceProductUuid);

}
