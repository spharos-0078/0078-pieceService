package com.pieceofcake.piece_service.trade.vo.out;

import com.pieceofcake.piece_service.trade.entity.TradeType;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class GetTradedHistoryListResponseVo {
    private String historyUuid;     // 거래내역 Uuid
    private Long piecePrice;        // 조각당 가격
    private TradeType tradeType;    // 거래 type
    private Integer quantity;       // 거래 수량
    private LocalDateTime createdAt;// 거래 시간
    private Long totalPrice;        // 총 가격 (quantity * piecePrice)

    @Builder
    public GetTradedHistoryListResponseVo(String historyUuid, Long piecePrice, TradeType tradeType, Integer quantity,
                                          LocalDateTime createdAt, Long totalPrice) {
        this.historyUuid = historyUuid;
        this.piecePrice = piecePrice;
        this.tradeType = tradeType;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.totalPrice = totalPrice;
    }
}