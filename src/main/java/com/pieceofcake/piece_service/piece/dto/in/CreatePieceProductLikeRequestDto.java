package com.pieceofcake.piece_service.piece.dto.in;

import com.pieceofcake.piece_service.piece.entity.LikedPieceProduct;
import com.pieceofcake.piece_service.piece.vo.in.CreatePieceProductLikeRequestVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreatePieceProductLikeRequestDto {
    private String pieceProductUuid;
    private String productUuid;
    private String memberUuid;

    @Builder
    public CreatePieceProductLikeRequestDto(String pieceProductUuid, String productUuid, String memberUuid) {
        this.pieceProductUuid = pieceProductUuid;
        this.productUuid = productUuid;
        this.memberUuid = memberUuid;
    }

    public static CreatePieceProductLikeRequestDto from(String memberUuid, CreatePieceProductLikeRequestVo createPieceProductLikeRequestVo) {
        return CreatePieceProductLikeRequestDto.builder()
                .memberUuid(memberUuid)
                .pieceProductUuid(createPieceProductLikeRequestVo.getPieceProductUuid())
                .productUuid(createPieceProductLikeRequestVo.getProductUuid())
                .build();
    }

    public LikedPieceProduct toEntity() {
        return LikedPieceProduct.builder()
                .memberUuid(memberUuid)
                .pieceProductUuid(pieceProductUuid)
                .productUuid(productUuid)
                .build();
    }
}
