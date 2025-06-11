package com.pieceofcake.piece_service.trade.vo.in;

import com.pieceofcake.piece_service.trade.entity.TradeStatus;
import com.pieceofcake.piece_service.trade.entity.TradeType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateTradeReservationReqtestVo {

    private String pieceProductUuid;
    private Long registeredPrice;
    private Integer desiredQuantity;
    private TradeType tradeType;
    private TradeStatus tradeStatus;

}
