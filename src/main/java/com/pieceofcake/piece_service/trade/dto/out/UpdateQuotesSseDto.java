package com.pieceofcake.piece_service.trade.dto.out;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Getter
@NoArgsConstructor
public class UpdateQuotesSseDto {
    private List<Long> askp;
    private List<Long> bidp;
    private List<Long> askpRsqn;
    private List<Long> bidRsqn;

    @Builder
    public UpdateQuotesSseDto(List<Long> askp, List<Long> bidp, List<Long> askpRsqn, List<Long> bidRsqn) {
        this.askp = askp;
        this.bidp = bidp;
        this.askpRsqn = askpRsqn;
        this.bidRsqn = bidRsqn;
    }
}
