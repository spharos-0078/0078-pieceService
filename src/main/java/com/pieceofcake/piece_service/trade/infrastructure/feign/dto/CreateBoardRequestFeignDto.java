package com.pieceofcake.piece_service.trade.infrastructure.feign.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateBoardRequestFeignDto {
    private String boardType;
    private String boardUuid;

    @Builder
    public CreateBoardRequestFeignDto(String boardType, String boardUuid) {
        this.boardType = boardType;
        this.boardUuid = boardUuid;
    }
}
