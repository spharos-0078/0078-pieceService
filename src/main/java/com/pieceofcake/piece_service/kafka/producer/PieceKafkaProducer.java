package com.pieceofcake.piece_service.kafka.producer;

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

    public void sendCreatePieceEvent(PieceEvent pieceEvent) {
        log.info("Sending Create PieceEvent: {}", pieceEvent);
        CompletableFuture<SendResult<String, PieceEvent>> future =
                productKafkaTemplate.send("create-piece-product", pieceEvent);
    }
}
