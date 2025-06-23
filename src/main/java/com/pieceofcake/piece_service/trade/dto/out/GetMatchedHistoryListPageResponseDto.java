package com.pieceofcake.piece_service.trade.dto.out;

import com.pieceofcake.piece_service.trade.entity.PieceMatchedHistory;
import com.pieceofcake.piece_service.trade.vo.out.GetMatchedHistoryListPageResponseVo;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class GetMatchedHistoryListPageResponseDto {
    private List<GetMatchedHistoryListResponseDto> tradeHistoryResponseDtoList;
    private long page;
    private long size;
    private boolean hasNext;
    private long totalPage;
    private long totalElements;

    @Builder
    public GetMatchedHistoryListPageResponseDto(List<GetMatchedHistoryListResponseDto> tradeHistoryResponseDtoList, long page,
                                                long size, boolean hasNext, long totalPage, long totalElements) {
        this.tradeHistoryResponseDtoList = tradeHistoryResponseDtoList;
        this.page = page;
        this.size = size;
        this.hasNext = hasNext;
        this.totalPage = totalPage;
        this.totalElements = totalElements;
    }

    public static GetMatchedHistoryListPageResponseDto from(Page<PieceMatchedHistory> page) {
        return GetMatchedHistoryListPageResponseDto.builder()
                .tradeHistoryResponseDtoList(page.getContent().stream()
                        .map(GetMatchedHistoryListResponseDto::from).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .hasNext(page.hasNext())
                .totalPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }


    public GetMatchedHistoryListPageResponseVo toVo() {
        return GetMatchedHistoryListPageResponseVo.builder()
                .tradeHistoryResponseVoList(tradeHistoryResponseDtoList
                        .stream()
                        .map(GetMatchedHistoryListResponseDto::toVo)
                        .toList())
                .page(page)
                .size(size)
                .hasNext(hasNext)
                .totalPage(totalPage)
                .totalElements(totalElements)
                .build();
    }
}
