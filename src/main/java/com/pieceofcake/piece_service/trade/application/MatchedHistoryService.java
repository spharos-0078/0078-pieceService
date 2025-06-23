package com.pieceofcake.piece_service.trade.application;


import com.pieceofcake.piece_service.trade.dto.in.GetMatchedHistoryListRequestDto;
import com.pieceofcake.piece_service.trade.dto.out.GetMatchedHistoryListPageResponseDto;

public interface MatchedHistoryService {
    GetMatchedHistoryListPageResponseDto getTradeHistoryList(GetMatchedHistoryListRequestDto dto);
}
