package com.pieceofcake.piece_service.trade.dto.in;

import com.fasterxml.jackson.core.ErrorReportConfiguration;
import com.pieceofcake.piece_service.trade.entity.TradeStatus;
import com.pieceofcake.piece_service.trade.entity.TradeType;
import com.pieceofcake.piece_service.trade.vo.in.CreateTradeReservationReqtestVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateTradeReservationRequestDto {

    private String pieceProductUuid;
    private Long registeredPrice;
    private Integer desiredQuantity;
    private TradeType tradeType;
    private TradeStatus tradeStatus;

    @Builder
    public CreateTradeReservationRequestDto(
            String pieceProductUuid,
            Long registeredPrice,
            Integer desiredQuantity,
            TradeType tradeType,
            TradeStatus tradeStatus
    ) {
        this.pieceProductUuid = pieceProductUuid;
        this.registeredPrice = registeredPrice;
        this.desiredQuantity = desiredQuantity;
        this.tradeType = tradeType;
        this.tradeStatus = tradeStatus;
    }

    public static CreateTradeReservationRequestDto from(
            CreateTradeReservationReqtestVo createTradeReservationReqtestVo,
            String memberUuid
    ) {
        return CreateTradeReservationRequestDto.builder()
                .pieceProductUuid(createTradeReservationReqtestVo.getPieceProductUuid())
                .registeredPrice(createTradeReservationReqtestVo.getRegisteredPrice())
                .desiredQuantity(createTradeReservationReqtestVo.getDesiredQuantity())
                .tradeType(createTradeReservationReqtestVo.getTradeType())
                .tradeStatus(createTradeReservationReqtestVo.getTradeStatus())
                .build();
    }
}
