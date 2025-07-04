package com.pieceofcake.piece_service.kafka.producer;

import com.pieceofcake.piece_service.kafka.event.AlertKafkaEvent;
import com.pieceofcake.piece_service.kafka.event.PieceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class PieceKafkaProducer {

    private final KafkaTemplate<String, PieceEvent> productKafkaTemplate;
    private final KafkaTemplate<String, AlertKafkaEvent> alertkafkaTemplate;

    public void sendCreatePieceEvent(PieceEvent pieceEvent) {
        log.info("Sending Create PieceEvent: {}", pieceEvent);
        CompletableFuture<SendResult<String, PieceEvent>> future =
                productKafkaTemplate.send("create-piece-product", pieceEvent);
    }

    public void sendSellPieceTradeAlertEvent(AlertKafkaEvent alertKafkaEvent) {
        log.info("Sending success sell piece: {}", alertKafkaEvent);
        CompletableFuture<SendResult<String, AlertKafkaEvent>> future =
                alertkafkaTemplate.send("sell-piece-success-alarm", alertKafkaEvent);
    }

    public void sendBuyPieceTradeAlertEvent(AlertKafkaEvent alertKafkaEvent) {
        log.info("Sending success buy piece: {}", alertKafkaEvent);
        CompletableFuture<SendResult<String, AlertKafkaEvent>> future =
                alertkafkaTemplate.send("buy-piece-success-alarm", alertKafkaEvent);
    }

    public void updatePiecePriceAlertEvent(AlertKafkaEvent alertKafkaEvent) {
        log.info("Sending update piece price: {}", alertKafkaEvent);
        CompletableFuture<SendResult<String, AlertKafkaEvent>> future =
                alertkafkaTemplate.send("update-piece-price-alarm", alertKafkaEvent);
    }
}
