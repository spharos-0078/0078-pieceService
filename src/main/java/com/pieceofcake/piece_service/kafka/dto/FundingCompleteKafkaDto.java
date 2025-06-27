package com.pieceofcake.piece_service.kafka.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FundingCompleteKafkaDto {
    private String fundingUuid;
    private String productUuid;
    private Long piecePrice;
    private Integer totalPieces;
    private Boolean isTrading;

    @Builder
    public FundingCompleteKafkaDto(
            String fundingUuid, String productUuid, Long piecePrice,
            Integer totalPieces, Boolean isTrading
    ) {
        this.fundingUuid = fundingUuid;
        this.productUuid = productUuid;
        this.piecePrice = piecePrice;
        this.totalPieces = totalPieces;
        this.isTrading = isTrading;
    }

    public Boolean isTrading() {
        return isTrading;
    }
}
