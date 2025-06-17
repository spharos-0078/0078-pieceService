package com.pieceofcake.piece_service.piece.vo.in;

import lombok.Getter;

@Getter
public class DistributePieceRequestVo {
    private String productUuid;
    private Integer pieceQuantity;
    private Boolean applyStatus;
}
