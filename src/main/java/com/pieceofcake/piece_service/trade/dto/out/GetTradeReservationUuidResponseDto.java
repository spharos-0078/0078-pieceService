package com.pieceofcake.piece_service.trade.dto.out;

import com.pieceofcake.piece_service.trade.entity.PieceTradeReservation;
import com.pieceofcake.piece_service.trade.vo.out.GetTradeReservationUuidResponseVo;
import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Nonnull
public class GetTradeReservationUuidResponseDto {
    private String reservationUuid;

    @Builder
    public GetTradeReservationUuidResponseDto(String reservationUuid) {
        this.reservationUuid = reservationUuid;
    }

    public static GetTradeReservationUuidResponseDto from(PieceTradeReservation reservation) {
        return GetTradeReservationUuidResponseDto.builder()
                .reservationUuid(reservation.getReservationUuid())
                .build();
    }

    public GetTradeReservationUuidResponseVo toVo() {
        return GetTradeReservationUuidResponseVo.builder()
                .reservationUuid(this.reservationUuid)
                .build();
    }
}
