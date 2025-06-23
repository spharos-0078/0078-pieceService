package com.pieceofcake.piece_service.trade.dto.in;

import com.pieceofcake.piece_service.trade.entity.PieceMatchedHistory;
import com.pieceofcake.piece_service.trade.entity.PieceTradeReservation;
import com.pieceofcake.piece_service.trade.entity.TradeType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CreateMatchedHistoryRequestDto {

    private String matchedUuid;
    private String pieceProductUuid;
    private Long piecePrice;
    private Integer matchedQuantity;
    private LocalDateTime matchedTime;
    private String memberUuid;
    private TradeType tradeType;

    @Builder
    public CreateMatchedHistoryRequestDto(String pieceProductUuid, Long piecePrice, Integer matchedQuantity,
                                          String memberUuid, TradeType tradeType, String matchedUuid, LocalDateTime matchedTime) {
        this.pieceProductUuid = pieceProductUuid;
        this.piecePrice = piecePrice;
        this.matchedQuantity = matchedQuantity;
        this.memberUuid = memberUuid;
        this.tradeType = tradeType;
        this.matchedUuid = matchedUuid;
        this.matchedTime = matchedTime;
    }

    public static CreateMatchedHistoryRequestDto of(
            PieceTradeReservation reservation,
            String matchedUuid,
            Long price,
            int matchedQuantity,
            String memberUuid,
            TradeType tradeType
    ) {
        return CreateMatchedHistoryRequestDto.builder()
                .matchedUuid(matchedUuid)
                .pieceProductUuid(reservation.getPieceProductUuid())
                .piecePrice(price)
                .matchedQuantity(matchedQuantity)
                .matchedTime(LocalDateTime.now()) // 또는 외부 주입
                .memberUuid(memberUuid)
                .tradeType(tradeType)
                .build();
    }

    public PieceMatchedHistory toEntity() {
        return PieceMatchedHistory.builder()
                .matchedUuid(matchedUuid)
                .pieceProductUuid(pieceProductUuid)
                .piecePrice(piecePrice)
                .matchedQuantity(matchedQuantity)
                .matchedTime(LocalDateTime.now())
                .memberUuid(memberUuid)
                .tradeType(tradeType)
                .build();
    }

}
