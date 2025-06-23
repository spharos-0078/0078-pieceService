package com.pieceofcake.piece_service.trade.dto.out;

import com.pieceofcake.piece_service.trade.entity.PieceMatchedHistory;
import com.pieceofcake.piece_service.trade.entity.TradeType;
import com.pieceofcake.piece_service.trade.vo.out.GetMatchedHistoryListResponseVo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GetMatchedHistoryListResponseDto {
    private String matchedUuid;
    private Long piecePrice;
    private TradeType tradeType;
    private Integer quantity;
    private LocalDateTime matchedTime;
    private Long totalPrice;

    @Builder
    public GetMatchedHistoryListResponseDto(String matchedUuid, Long piecePrice, TradeType tradeType, Integer quantity,
                                            LocalDateTime matchedTime, Long totalPrice) {
        this.matchedUuid = matchedUuid;
        this.piecePrice = piecePrice;
        this.tradeType = tradeType;
        this.quantity = quantity;
        this.matchedTime = matchedTime;
        this.totalPrice = totalPrice;
    }

    public static GetMatchedHistoryListResponseDto from(PieceMatchedHistory pieceMatchedHistory) {
        return GetMatchedHistoryListResponseDto.builder()
                .matchedUuid(pieceMatchedHistory.getMatchedUuid())
                .piecePrice(pieceMatchedHistory.getPiecePrice())
                .tradeType(pieceMatchedHistory.getTradeType())
                .quantity(pieceMatchedHistory.getMatchedQuantity())
                .matchedTime(pieceMatchedHistory.getMatchedTime())
                .totalPrice(pieceMatchedHistory.getPiecePrice() * pieceMatchedHistory.getMatchedQuantity())
                .build();
    }

    public GetMatchedHistoryListResponseVo toVo() {
        return GetMatchedHistoryListResponseVo.builder()
                .matchedUuid(matchedUuid)
                .piecePrice(piecePrice)
                .tradeType(tradeType)
                .quantity(quantity)
                .matchedTime(matchedTime)
                .totalPrice(totalPrice)
                .build();
    }
}
