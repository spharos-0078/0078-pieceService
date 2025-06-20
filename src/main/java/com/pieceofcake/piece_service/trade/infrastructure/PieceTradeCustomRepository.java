package com.pieceofcake.piece_service.trade.infrastructure;

import com.pieceofcake.piece_service.trade.dto.out.GetTradedHistoryListResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PieceTradeCustomRepository {
    Page<GetTradedHistoryListResponseDto> findDistinctHistoriesByPieceProductUuidAndMemberUuid(String pieceProductUuid, String memberUuid, Pageable pageable);
}
