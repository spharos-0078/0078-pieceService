package com.pieceofcake.piece_service.trade.scheduler;

import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class TradingTimeChecker {

    private static final LocalTime START_TIME = LocalTime.of(0, 0);   // 자정
    private static final LocalTime END_TIME = LocalTime.of(22, 0);    // 밤 10시

    public boolean isMarketOpen() {
        LocalTime now = LocalTime.now();
        return !now.isBefore(START_TIME) && now.isBefore(END_TIME);
    }

}
