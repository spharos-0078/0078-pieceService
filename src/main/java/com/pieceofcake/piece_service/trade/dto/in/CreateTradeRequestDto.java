package com.pieceofcake.piece_service.trade.dto.in;

import com.pieceofcake.piece_service.trade.entity.PieceTradeReservation;
import com.pieceofcake.piece_service.trade.entity.TradeStatus;
import com.pieceofcake.piece_service.trade.entity.TradeType;
import com.pieceofcake.piece_service.trade.vo.in.CreateTradeRequestVo;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class CreateTradeRequestDto {

    private String memberUuid;
    private String pieceProductUuid;
    private Long registeredPrice;
    private Integer desiredQuantity;
    private TradeType tradeType;

    private Integer remainingQuantity;
    private TradeStatus tradeStatus;

    @Builder
    public CreateTradeRequestDto(
            String memberUuid, String pieceProductUuid, Long registeredPrice, Integer desiredQuantity,
            TradeType tradeType, Integer remainingQuantity, TradeStatus tradeStatus
    ) {
        this.memberUuid = memberUuid;
        this.pieceProductUuid = pieceProductUuid;
        this.registeredPrice = registeredPrice;
        this.desiredQuantity = desiredQuantity;
        this.tradeType = tradeType;
        this.remainingQuantity = remainingQuantity;
        this.tradeStatus = tradeStatus;
    }

    public static CreateTradeRequestDto fromBuy(String memberUuid, CreateTradeRequestVo vo) {
        return CreateTradeRequestDto.builder()
                .memberUuid(memberUuid)
                .pieceProductUuid(vo.getPieceProductUuid())
                .registeredPrice(vo.getRegisteredPrice())
                .desiredQuantity(vo.getDesiredQuantity())
                .tradeType(TradeType.BUY)
                .remainingQuantity(vo.getDesiredQuantity())
                .tradeStatus(TradeStatus.WAITING)
                .build();
    }

    public static CreateTradeRequestDto fromSell(String memberUuid, CreateTradeRequestVo vo) {
        return CreateTradeRequestDto.builder()
                .memberUuid(memberUuid)
                .pieceProductUuid(vo.getPieceProductUuid())
                .registeredPrice(vo.getRegisteredPrice())
                .desiredQuantity(vo.getDesiredQuantity())
                .tradeType(TradeType.SELL)
                .remainingQuantity(vo.getDesiredQuantity())
                .tradeStatus(TradeStatus.WAITING)
                .build();
    }


    public PieceTradeReservation toBuyEntity(String memberUuid) {
        return PieceTradeReservation.builder()
                .reservationUuid(UUID.randomUUID().toString().substring(0, 32))
                .memberUuid(memberUuid)
                .pieceProductUuid(pieceProductUuid)
                .registeredPrice(registeredPrice)
                .desiredQuantity(desiredQuantity)
                .tradeType(tradeType.BUY)
                .remainingQuantity(this.desiredQuantity)
                .tradeStatus(tradeStatus.WAITING)
                .build();
    }

    public PieceTradeReservation toSellEntity(String memberUuid) {
        return PieceTradeReservation.builder()
                .reservationUuid(UUID.randomUUID().toString().substring(0, 32))
                .memberUuid(memberUuid)
                .pieceProductUuid(pieceProductUuid)
                .registeredPrice(registeredPrice)
                .desiredQuantity(desiredQuantity)
                .tradeType(tradeType.SELL)
                .remainingQuantity(this.desiredQuantity)
                .tradeStatus(tradeStatus.WAITING)
                .build();
    }
}
