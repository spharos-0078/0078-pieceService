package com.pieceofcake.piece_service.trade.vo.out;

import com.pieceofcake.piece_service.trade.entity.TradeStatus;
import com.pieceofcake.piece_service.trade.entity.TradeType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GetTradeReservationResponseVo {
    private String reservationUuid;
    private String pieceProductUuid;
    private Long registeredPrice;
    private Integer desiredQuantity;
    private TradeType tradeType;
    private TradeStatus tradeStatus;

    @Builder
    public GetTradeReservationResponseVo(
            String reservationUuid, String pieceProductUuid, Long registeredPrice,
            Integer desiredQuantity, TradeType tradeType, TradeStatus tradeStatus
    ) {
        this.reservationUuid = reservationUuid;
        this.pieceProductUuid = pieceProductUuid;
        this.registeredPrice = registeredPrice;
        this.desiredQuantity = desiredQuantity;
        this.tradeType = tradeType;
        this.tradeStatus = tradeStatus;
    }
}
