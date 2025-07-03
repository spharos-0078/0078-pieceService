package com.pieceofcake.piece_service.trade.vo.out;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GetTradeReservationUuidResponseVo {
    private String reservationUuid;

    @Builder
    public GetTradeReservationUuidResponseVo(String reservationUuid) {
        this.reservationUuid = reservationUuid;
    }
}
