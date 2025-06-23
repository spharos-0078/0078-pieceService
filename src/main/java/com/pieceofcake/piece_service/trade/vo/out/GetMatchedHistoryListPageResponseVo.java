package com.pieceofcake.piece_service.trade.vo.out;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class GetMatchedHistoryListPageResponseVo {
    private List<GetMatchedHistoryListResponseVo> tradeHistoryResponseVoList;
    private long page;
    private long size;
    private boolean hasNext;
    private long totalPage;
    private long totalElements;

    @Builder
    public GetMatchedHistoryListPageResponseVo(List<GetMatchedHistoryListResponseVo> tradeHistoryResponseVoList, long page,
                                               long size, boolean hasNext, long totalPage, long totalElements) {
        this.tradeHistoryResponseVoList = tradeHistoryResponseVoList;
        this.page = page;
        this.size = size;
        this.hasNext = hasNext;
        this.totalPage = totalPage;
        this.totalElements = totalElements;
    }
}
