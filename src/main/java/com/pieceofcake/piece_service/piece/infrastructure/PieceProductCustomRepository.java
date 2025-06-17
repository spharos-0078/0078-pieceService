package com.pieceofcake.piece_service.piece.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PieceProductCustomRepository {
    Page<String>  findProductUuidsByTradingStatus(Pageable pageable, Boolean isTrading);
}
