package com.pieceofcake.piece_service.piece.infrastructure;

import com.pieceofcake.piece_service.piece.entity.Piece;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PieceJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public void savePieces(List<Piece> pieces) {
        String sql = "INSERT INTO piece (piece_uuid, serial_number, product_uuid, created_at, updated_at) " +
                "VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        jdbcTemplate.batchUpdate(sql,
                pieces,
                pieces.size(),
                (PreparedStatement ps, Piece piece) -> {
                    ps.setString(1, piece.getPieceUuid());
                    ps.setInt(2, piece.getSerialNumber());
                    ps.setString(3, piece.getProductUuid());
                });
    }

}
