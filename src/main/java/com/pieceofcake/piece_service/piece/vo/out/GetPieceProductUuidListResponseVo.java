package com.pieceofcake.piece_service.piece.vo.out;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class GetPieceProductUuidListResponseVo {
    private List<String> produtUuidList;
    private long page;
    private long size;
    private boolean hasNext;
    private long totalPage;
    private long totalElements;

    @Builder
    public GetPieceProductUuidListResponseVo(List<String> produtUuidList, long page, long size, boolean hasNext,
                                             long totalPage, long totalElements) {
        this.produtUuidList = produtUuidList;
        this.page = page;
        this.size = size;
        this.hasNext = hasNext;
        this.totalPage = totalPage;
        this.totalElements = totalElements;
    }
}
