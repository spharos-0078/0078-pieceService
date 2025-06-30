package com.pieceofcake.piece_service.trade.dto.out;

import com.pieceofcake.piece_service.trade.entity.PieceTradeReservation;
import com.pieceofcake.piece_service.trade.entity.TradeStatus;
import com.pieceofcake.piece_service.trade.entity.TradeType;
import com.pieceofcake.piece_service.trade.vo.out.GetTradeReservationResponseVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GetTradeReservationResponseDto {
    private String reservationUuid;
    private String pieceProductUuid;
    private Long registeredPrice;
    private Integer desiredQuantity;
    private TradeType tradeType;
    private TradeStatus tradeStatus;

    @Builder
    public GetTradeReservationResponseDto(
            String reservationUuid, String pieceProductUuid,
            Long registeredPrice, Integer desiredQuantity,
            TradeType tradeType, TradeStatus tradeStatus
    ) {
        this.reservationUuid = reservationUuid;
        this.pieceProductUuid = pieceProductUuid;
        this.registeredPrice = registeredPrice;
        this.desiredQuantity = desiredQuantity;
        this.tradeType = tradeType;
        this.tradeStatus = tradeStatus;
    }

    public static GetTradeReservationResponseDto from(PieceTradeReservation reservation) {
        return GetTradeReservationResponseDto.builder()
                .reservationUuid(reservation.getReservationUuid())
                .pieceProductUuid(reservation.getPieceProductUuid())
                .registeredPrice(reservation.getRegisteredPrice())
                .desiredQuantity(reservation.getDesiredQuantity())
                .tradeType(reservation.getTradeType())
                .tradeStatus(reservation.getTradeStatus())
                .build();
    }

    public GetTradeReservationResponseVo toVo() {
        return GetTradeReservationResponseVo.builder()
                .reservationUuid(reservationUuid)
                .pieceProductUuid(pieceProductUuid)
                .registeredPrice(registeredPrice)
                .desiredQuantity(desiredQuantity)
                .tradeType(tradeType)
                .tradeStatus(tradeStatus)
                .build();
    }
}
