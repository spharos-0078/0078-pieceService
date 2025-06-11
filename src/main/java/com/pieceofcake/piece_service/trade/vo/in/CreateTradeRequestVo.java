package com.pieceofcake.piece_service.trade.vo.in;

import com.pieceofcake.piece_service.trade.entity.TradeStatus;
import com.pieceofcake.piece_service.trade.entity.TradeType;
import lombok.Getter;

@Getter
public class CreateTradeRequestVo {

    private String pieceProductUuid;
    private Long registeredPrice;
    private Integer desiredQuantity;
    private Integer remainingQuantity;
    private TradeType tradeType;
    private TradeStatus tradeStatus;

}
