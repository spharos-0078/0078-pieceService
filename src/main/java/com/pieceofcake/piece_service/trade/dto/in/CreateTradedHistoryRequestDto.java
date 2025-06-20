package com.pieceofcake.piece_service.trade.dto.in;

import com.pieceofcake.piece_service.trade.entity.PieceTradeReservation;
import com.pieceofcake.piece_service.trade.entity.PieceTradedHistory;
import com.pieceofcake.piece_service.trade.entity.TradeType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CreateTradedHistoryRequestDto {

    private String matchedUuid;
    private String pieceProductUuid;
    private String pieceUuid;
    private Long piecePrice;
    private TradeType tradeType;
    private String memberUuid;

    @Builder
    public CreateTradedHistoryRequestDto(
            String matchedUuid, String pieceProductUuid, String pieceUuid,
            Long piecePrice, TradeType tradeType, String memberUuid
    ) {
        this.matchedUuid = matchedUuid;
        this.pieceProductUuid = pieceProductUuid;
        this.pieceUuid = pieceUuid;
        this.piecePrice = piecePrice;
        this.tradeType = tradeType;
        this.memberUuid = memberUuid;
    }

    public static CreateTradedHistoryRequestDto of(PieceTradeReservation reservation, String pieceUuid, TradeType tradeType, String memberUuid) {
        return builder()
                .matchedUuid(null)
                .pieceProductUuid(reservation.getPieceProductUuid())
                .pieceUuid(pieceUuid)
                .piecePrice(reservation.getRegisteredPrice())
                .tradeType(tradeType)
                .memberUuid(memberUuid)
                .build();
    }

        public PieceTradedHistory toEntity() {
            return PieceTradedHistory.builder()
                    .matchedUuid(matchedUuid != null ? matchedUuid : UUID.randomUUID().toString().substring(0, 32))
                    .pieceUuid(pieceUuid)
                    .tradeType(tradeType)
                    .memberUuid(memberUuid)
                    .build();
        }
}