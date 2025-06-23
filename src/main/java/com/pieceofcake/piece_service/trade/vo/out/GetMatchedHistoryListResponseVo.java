package com.pieceofcake.piece_service.trade.vo.out;

import com.pieceofcake.piece_service.trade.entity.TradeType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GetMatchedHistoryListResponseVo {
    private String matchedUuid;     // 거래내역 Uuid
    private Long piecePrice;        // 조각당 가격
    private TradeType tradeType;    // 거래 type
    private Integer quantity;       // 거래 수량
    private LocalDateTime matchedTime;// 거래 시간
    private Long totalPrice;        // 총 가격 (quantity * piecePrice)

    @Builder
    public GetMatchedHistoryListResponseVo(String matchedUuid, Long piecePrice, TradeType tradeType, Integer quantity,
                                           LocalDateTime matchedTime, Long totalPrice) {
        this.matchedUuid = matchedUuid;
        this.piecePrice = piecePrice;
        this.tradeType = tradeType;
        this.quantity = quantity;
        this.matchedTime = matchedTime;
        this.totalPrice = totalPrice;
    }
}