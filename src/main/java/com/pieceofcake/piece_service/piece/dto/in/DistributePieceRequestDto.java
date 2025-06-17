package com.pieceofcake.piece_service.piece.dto.in;

import com.pieceofcake.piece_service.piece.vo.in.DistributePieceRequestVo;
import lombok.Builder;
import lombok.Getter;

@Getter
public class DistributePieceRequestDto {
    private String productUuid;
    private Integer pieceQuantity;
    private Boolean applyStatus;
    private String memberUuid;

    @Builder
    public DistributePieceRequestDto(String productUuid, Integer pieceQuantity, Boolean applyStatus, String memberUuid) {
        this.productUuid = productUuid;
        this.pieceQuantity = pieceQuantity;
        this.applyStatus = applyStatus;
        this.memberUuid = memberUuid;
    }

    public static DistributePieceRequestDto of(String memberUuid, DistributePieceRequestVo vo){
        return DistributePieceRequestDto.builder()
                .productUuid(vo.getProductUuid())
                .pieceQuantity(vo.getPieceQuantity())
                .applyStatus(vo.getApplyStatus())
                .memberUuid(memberUuid)
                .build();
    }
}
