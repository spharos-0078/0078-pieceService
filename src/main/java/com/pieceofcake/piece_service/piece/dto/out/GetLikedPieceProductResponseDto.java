package com.pieceofcake.piece_service.piece.dto.out;

import com.pieceofcake.piece_service.piece.entity.LikedPieceProduct;
import com.pieceofcake.piece_service.piece.vo.out.GetLikedPieceProductResponseVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GetLikedPieceProductResponseDto {
    private String pieceProductUuid;
    private String productUuid;
    private String memberUuid;

    @Builder
    public GetLikedPieceProductResponseDto(String pieceProductUuid, String productUuid, String memberUuid) {
        this.pieceProductUuid = pieceProductUuid;
        this.productUuid = productUuid;
        this.memberUuid = memberUuid;
    }

    public static GetLikedPieceProductResponseDto from(LikedPieceProduct likedPieceProduct) {
        return GetLikedPieceProductResponseDto.builder()
                .pieceProductUuid(likedPieceProduct.getPieceProductUuid())
                .productUuid(likedPieceProduct.getProductUuid())
                .memberUuid(likedPieceProduct.getMemberUuid())
                .build();
    }

    public GetLikedPieceProductResponseVo toVo() {
        return GetLikedPieceProductResponseVo.builder()
                .pieceProductUuid(this.pieceProductUuid)
                .productUuid(this.productUuid)
                .memberUuid(this.memberUuid)
                .build();
    }
}
