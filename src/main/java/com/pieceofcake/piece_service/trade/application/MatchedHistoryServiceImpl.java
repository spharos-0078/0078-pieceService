package com.pieceofcake.piece_service.trade.application;

import com.pieceofcake.piece_service.trade.dto.in.GetMatchedHistoryListRequestDto;
import com.pieceofcake.piece_service.trade.dto.out.GetMatchedHistoryListPageResponseDto;
import com.pieceofcake.piece_service.trade.infrastructure.PieceMatchedHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MatchedHistoryServiceImpl implements MatchedHistoryService {

    private final PieceMatchedHistoryRepository pieceMatchedHistoryRepository;

    @Override
    public GetMatchedHistoryListPageResponseDto getTradeHistoryList(GetMatchedHistoryListRequestDto dto) {
        return GetMatchedHistoryListPageResponseDto.from(pieceMatchedHistoryRepository
                .findAllByMemberUuidAndPieceProductUuid(
                        dto.getMemberUuid(), dto.getPieceProductUuid(), dto.getPageable()
                )
        );
    }
}
