package com.pieceofcake.piece_service.trade.dto.out;

import com.pieceofcake.piece_service.trade.dto.in.GetTradedHistoryListRequestDto;
import com.pieceofcake.piece_service.trade.vo.out.GetTradedHistoryListPageResponseVo;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class GetTradedHistoryListPageResponseDto {
    private List<GetTradedHistoryListResponseDto> tradeHistoryResponseDtoList;
    private long page;
    private long size;
    private boolean hasNext;
    private long totalPage;
    private long totalElements;

    @Builder
    public GetTradedHistoryListPageResponseDto(List<GetTradedHistoryListResponseDto> tradeHistoryResponseDtoList, long page,
                                               long size, boolean hasNext, long totalPage, long totalElements) {
        this.tradeHistoryResponseDtoList = tradeHistoryResponseDtoList;
        this.page = page;
        this.size = size;
        this.hasNext = hasNext;
        this.totalPage = totalPage;
        this.totalElements = totalElements;
    }

    public static GetTradedHistoryListPageResponseDto from(Page<GetTradedHistoryListResponseDto> page) {
        return GetTradedHistoryListPageResponseDto.builder()
                .tradeHistoryResponseDtoList(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .hasNext(page.hasNext())
                .totalPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }


    public GetTradedHistoryListPageResponseVo toVo() {
        return GetTradedHistoryListPageResponseVo.builder()
                .tradeHistoryResponseVoList(tradeHistoryResponseDtoList
                        .stream()
                        .map(GetTradedHistoryListResponseDto::toVo)
                        .toList())
                .page(page)
                .size(size)
                .hasNext(hasNext)
                .totalPage(totalPage)
                .totalElements(totalElements)
                .build();
    }
}
