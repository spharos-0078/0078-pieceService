package com.pieceofcake.piece_service.trade.scheduler;

import com.pieceofcake.piece_service.trade.entity.PieceTradeReservation;
import com.pieceofcake.piece_service.trade.entity.TradeStatus;
import com.pieceofcake.piece_service.trade.infrastructure.TradeReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class TradeClosingScheduler {

    private final TradeReservationRepository tradeReservationRepository;

    /**
     * 매일 22:01에 장 종료된 예약 자동 취소
     */
    @Scheduled(cron = "0 1 22 * * *", zone = "Asia/Seoul")
    @Transactional
    public void cancelUnmatchedReservations() {
        List<PieceTradeReservation> unmatched =
                tradeReservationRepository.findByTradeStatus(TradeStatus.WAITING);

        unmatched.forEach(PieceTradeReservation::cancel);
        tradeReservationRepository.saveAll(unmatched);
    }

}
