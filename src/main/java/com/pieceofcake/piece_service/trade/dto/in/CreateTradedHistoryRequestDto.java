package com.pieceofcake.piece_service.trade.dto.in;

import com.pieceofcake.piece_service.trade.entity.PieceTradeReservation;
import com.pieceofcake.piece_service.trade.entity.PieceTradedHistory;
import com.pieceofcake.piece_service.trade.entity.TradeType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CreateTradedHistoryRequestDto {

    private String pieceProductUuid;
    private String pieceUuid;
    private Long price;
    private TradeType tradeType;
    private String memberUuid;

    @Builder
    public CreateTradedHistoryRequestDto(
            String pieceProductUuid, String pieceUuid,
            Long price, TradeType tradeType, String memberUuid
    ) {
        this.pieceProductUuid = pieceProductUuid;
        this.pieceUuid = pieceUuid;
        this.price = price;
        this.tradeType = tradeType;
        this.memberUuid = memberUuid;
    }

    public static CreateTradedHistoryRequestDto of(PieceTradeReservation reservation, String pieceUuid, TradeType tradeType, String memberUuid) {
        return CreateTradedHistoryRequestDto.builder()
                .pieceProductUuid(reservation.getPieceProductUuid())
                .pieceUuid(pieceUuid)
                .price(reservation.getRegisteredPrice())
                .tradeType(tradeType)
                .memberUuid(memberUuid)
                .build();
    }

    public PieceTradedHistory toEntity() {
        return PieceTradedHistory.builder()
                .historyUuid(UUID.randomUUID().toString().substring(0, 32))
                .pieceProductUuid(pieceProductUuid)
                .pieceUuid(pieceUuid)
                .price(price)
                .tradeType(tradeType)
                .memberUuid(memberUuid)
                .build();
    }
}
