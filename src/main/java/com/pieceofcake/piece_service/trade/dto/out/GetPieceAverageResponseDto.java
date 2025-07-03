package com.pieceofcake.piece_service.trade.dto.out;

import com.pieceofcake.piece_service.trade.entity.OwnedPieceAverage;
import com.pieceofcake.piece_service.trade.vo.out.GetPieceAverageResponseVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GetPieceAverageResponseDto {
    private String pieceProductUuid;
    private Integer totalQuantity;
    private Long totalAmount;
    private Long averagePrice;

    @Builder
    public GetPieceAverageResponseDto(
            String pieceProductUuid, Integer totalQuantity,
            Long totalAmount, Long averagePrice
    ) {
        this.pieceProductUuid = pieceProductUuid;
        this.totalQuantity = totalQuantity;
        this.totalAmount = totalAmount;
        this.averagePrice = averagePrice;
    }

    public static GetPieceAverageResponseDto from(OwnedPieceAverage ownedPieceAverage) {
        return GetPieceAverageResponseDto.builder()
                .pieceProductUuid(ownedPieceAverage.getPieceProductUuid())
                .totalQuantity(ownedPieceAverage.getTotalQuantity())
                .totalAmount(ownedPieceAverage.getTotalAmount())
                .averagePrice(ownedPieceAverage.getAveragePrice())
                .build();
    }

    public GetPieceAverageResponseVo toVo() {
        return GetPieceAverageResponseVo.builder()
                .pieceProductUuid(pieceProductUuid)
                .totalQuantity(totalQuantity)
                .totalAmount(totalAmount)
                .averagePrice(averagePrice)
                .build();
    }
}
