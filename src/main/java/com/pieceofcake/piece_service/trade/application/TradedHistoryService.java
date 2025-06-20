package com.pieceofcake.piece_service.trade.application;


import com.pieceofcake.piece_service.trade.dto.in.GetTradedHistoryListRequestDto;
import com.pieceofcake.piece_service.trade.dto.out.GetTradedHistoryListPageResponseDto;

public interface TradedHistoryService {
    GetTradedHistoryListPageResponseDto getTradeHistoryList(GetTradedHistoryListRequestDto dto);
}
