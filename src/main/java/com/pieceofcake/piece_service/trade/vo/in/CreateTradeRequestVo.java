package com.pieceofcake.piece_service.trade.vo.in;

import com.pieceofcake.piece_service.trade.entity.TradeStatus;
import com.pieceofcake.piece_service.trade.entity.TradeType;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateTradeRequestVo {

    private String pieceProductUuid;
    private Long registeredPrice;
    private Integer desiredQuantity;
    private TradeType tradeType;

    private Integer remainingQuantity;
    private TradeStatus tradeStatus;

    @Builder
    public CreateTradeRequestVo(
            String pieceProductUuid, Long registeredPrice, Integer desiredQuantity,
            TradeType tradeType, Integer remainingQuantity, TradeStatus tradeStatus
    ) {
        this.pieceProductUuid = pieceProductUuid;
        this.registeredPrice = registeredPrice;
        this.desiredQuantity = desiredQuantity;
        this.tradeType = tradeType;
        this.remainingQuantity = remainingQuantity;
        this.tradeStatus = tradeStatus;
    }
}
