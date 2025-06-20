package com.pieceofcake.piece_service.trade.dto.out;

import com.pieceofcake.piece_service.trade.entity.PieceTradedHistory;
import com.pieceofcake.piece_service.trade.entity.TradeType;
import com.pieceofcake.piece_service.trade.vo.out.GetTradedHistoryListResponseVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Date;
import java.time.LocalDate;

@Getter
//@AllArgsConstructor // JPA의 new DTO(...)를 위해 모든 필드를 인자로 받는 생성자 생성
//@Builder
public class GetTradedHistoryListResponseDto {
    private String historyUuid;
    private Long piecePrice;
    private TradeType tradeType;
    private Integer quantity;
    private LocalDateTime createdAt;
    private Long totalPrice;

    @Builder
    public GetTradedHistoryListResponseDto(String historyUuid, Long piecePrice, TradeType tradeType, Integer quantity,
                                           LocalDateTime createdAt, Long totalPrice) {
        this.historyUuid = historyUuid;
        this.piecePrice = piecePrice;
        this.tradeType = tradeType;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.totalPrice = totalPrice;
    }

//    public static GetTradedHistoryListResponseDto from(PieceTradedHistory pieceTradedHistory) {
//        return GetTradedHistoryListResponseDto.builder()
//                .historyUuid(pieceTradedHistory.getHistoryUuid())
//                .piecePrice(pieceTradedHistory.getPrice())
//                .tradeType(pieceTradedHistory.getTradeType())
//                .quantity(pieceTradedHistory.getQuantity())
//                .createdAt(pieceTradedHistory.getCreatedAt())
//                .totalPrice(pieceTradedHistory.getPrice() * pieceTradedHistory.getQuantity())
//                .build();
//    }

    public GetTradedHistoryListResponseVo toVo() {
        return GetTradedHistoryListResponseVo.builder()
                .historyUuid(historyUuid)
                .piecePrice(piecePrice)
                .tradeType(tradeType)
                .quantity(quantity)
                .createdAt(createdAt)
                .totalPrice(totalPrice)
                .build();
    }
}
