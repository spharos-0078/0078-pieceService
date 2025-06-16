package com.pieceofcake.piece_service.piece.dto.out;

import com.pieceofcake.piece_service.piece.vo.out.GetPieceProductUuidListResponseVo;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;


import java.util.List;

@Getter
public class GetPieceProductUuidListResponseDto {
    private List<String> produtUuidList;
    private long page;
    private long size;
    private boolean hasNext;
    private long totalPage;
    private long totalElements;

    @Builder
    public GetPieceProductUuidListResponseDto(List<String> produtUuidList, long page, long size, boolean hasNext,
                                              long totalPage, long totalElements) {
        this.produtUuidList = produtUuidList;
        this.page = page;
        this.size = size;
        this.hasNext = hasNext;
        this.totalPage = totalPage;
        this.totalElements = totalElements;
    }

    public static GetPieceProductUuidListResponseDto from(Page<String> page){
        return GetPieceProductUuidListResponseDto.builder()
                .produtUuidList(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .hasNext(page.hasNext())
                .totalPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }

    public GetPieceProductUuidListResponseVo toVo(){
        return GetPieceProductUuidListResponseVo.builder()
                .produtUuidList(produtUuidList)
                .page(page)
                .size(size)
                .hasNext(hasNext)
                .totalPage(totalPage)
                .totalElements(totalElements)
                .build();
    }
}
