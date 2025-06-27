package com.pieceofcake.piece_service.kafka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pieceofcake.piece_service.kafka.application.FundingCompleteService;
import com.pieceofcake.piece_service.kafka.dto.FundingCompleteKafkaDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaConsumerController {

    private final ObjectMapper objectMapper;
    private final FundingCompleteService fundingCompleteService;

    @KafkaListener(topics = "complete-funding", groupId = "piece-service-group")
    public void consumeFundingComplete(String message) {
        try {
            FundingCompleteKafkaDto fundingCompleteKafkaDto = objectMapper.readValue(message, FundingCompleteKafkaDto.class);
            log.info("[📩 Kafka 수신 완료] {}", fundingCompleteKafkaDto);

            fundingCompleteService.processFundingComplete(fundingCompleteKafkaDto);
        } catch (Exception e) {
            log.error("[❌ Kafka 처리 실패] message: {}", message, e);
        }
    }

}
