package com.pieceofcake.piece_service.trade.application;

import com.pieceofcake.piece_service.trade.dto.in.GetTradedHistoryListRequestDto;
import com.pieceofcake.piece_service.trade.dto.out.GetTradedHistoryListPageResponseDto;
import com.pieceofcake.piece_service.trade.infrastructure.PieceTradeCustomImplRepository;
import com.pieceofcake.piece_service.trade.infrastructure.TradedHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TradedHistoryServiceImpl implements TradedHistoryService {

    private final PieceTradeCustomImplRepository pieceTradeCustomRepository;
//    private final TradedHistoryRepository tradedHistoryRepository;
    @Override
    public GetTradedHistoryListPageResponseDto getTradeHistoryList(GetTradedHistoryListRequestDto dto) {
        return GetTradedHistoryListPageResponseDto.from(pieceTradeCustomRepository
                .findDistinctHistoriesByPieceProductUuidAndMemberUuid(
                        dto.getPieceProductUuid(), dto.getMemberUuid(), dto.getPageable()
                )
        );
    }
}
