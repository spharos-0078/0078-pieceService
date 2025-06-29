package com.pieceofcake.piece_service.trade.infrastructure.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisPublisher {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 거래량 집계용 데이터 전송
     *
     * Key: pieceVolume:{조각상품UUID}:{yyyy-MM-dd}
     * Value (List): {"pieceProductUuid":"...", "piecePrice":3000, "matchedQuantity":1, "matchedTime":"yyyy-MM-dd HH:mm:ss"}
     */
    public void publishTradeVolume(String pieceProductUuid, long piecePrice, int matchedQuantity, LocalDateTime matchedTime) {
        String date = matchedTime.toLocalDate().toString();
        String formattedTime = matchedTime.format(DATETIME_FORMATTER);

        String key = String.format("pieceVolume:%s:%s", pieceProductUuid, date);

        Map<String, Object> payload = new HashMap<>();
        payload.put("pieceProductUuid", pieceProductUuid);
        payload.put("piecePrice", piecePrice);
        payload.put("matchedQuantity", matchedQuantity);
        payload.put("matchedTime", formattedTime);

        try {
            String value = objectMapper.writeValueAsString(payload);
            redisTemplate.opsForList().leftPush(key, value);
        } catch (JsonProcessingException e) {
            log.error("[RedisPublisher] 거래량 집계 JSON 직렬화 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * [2] 호가창 데이터 전송 (예약 발생 시)
     * Key (Hash): pieceOrderbook:{조각상품UUID}:{buy|sell} → price : quantity (누적)
     * Key (List): pieceOrderLog:{조각상품UUID}:{buy|sell} → 예약 로그
     */
    public void publishOrderBook(String pieceProductUuid, long price, int quantity, String tradeType) {
        String normalizedTradeType = tradeType.toLowerCase();

        // 1. 수량 누적용 Hash 저장
        String orderBookKey = String.format("pieceOrderbook:%s:%s", pieceProductUuid, normalizedTradeType);
        redisTemplate.opsForHash().increment(orderBookKey, String.valueOf(price), quantity);

        // 2. 호가 로그 저장용 List 저장
        String logKey = String.format("pieceOrderLog:%s:%s", pieceProductUuid, normalizedTradeType);

        Map<String, Object> logPayload = new HashMap<>();
        logPayload.put("pieceProductUuid", pieceProductUuid);
        logPayload.put("tradeType", tradeType.toUpperCase());
        logPayload.put("price", price);
        logPayload.put("quantity", quantity);
        logPayload.put("timestamp", LocalDateTime.now().format(DATETIME_FORMATTER));

        try {
            String logValue = objectMapper.writeValueAsString(logPayload);
            redisTemplate.opsForList().leftPush(logKey, logValue);
        } catch (JsonProcessingException e) {
            log.error("[RedisPublisher] 호가 로그 JSON 직렬화 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * [3] 호가 정보 롤백 (체결로 인해 수량 차감 필요할 때)
     */
    public void rollbackOrderBook(String pieceProductUuid, long price, int quantity, String tradeType) {
        String key = String.format("pieceOrderbook:%s:%s", pieceProductUuid, tradeType.toLowerCase());
        redisTemplate.opsForHash().increment(key, String.valueOf(price), -quantity);
    }

    /**
     * [4] 거래 가능 시간 (00:00~22:00) 검증 함수
     */
    public void validateMarketOpen() {
        LocalTime now = LocalTime.now();
        if (now.isAfter(LocalTime.of(22, 0)) || now.isBefore(LocalTime.MIDNIGHT)) {
            throw new IllegalStateException("현재는 거래 가능한 시간이 아닙니다. (00:00~22:00)");
        }
    }

    /**
     * [5] Redis Pub/Sub 이벤트 발행
     */
    public void publishRedisEvent(String channel, Object message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(channel, json);
            log.info("[RedisPublisher] Redis 이벤트 발행: channel={}, payload={}", channel, json);
        } catch (JsonProcessingException e) {
            log.error("Redis 이벤트 직렬화 실패", e);
        }
    }

    public void publishTradeReservedEvent(String tradeType, String memberUuid,
                                          String pieceProductUuid, long price, int quantity, LocalDateTime timestamp) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", tradeType.toUpperCase());
        payload.put("memberUuid", memberUuid);
        payload.put("pieceProductUuid", pieceProductUuid);
        payload.put("price", price);
        payload.put("quantity", quantity);
        payload.put("timestamp", timestamp.toString());

        String channel = String.format("piece.orderbook.%s", pieceProductUuid);
        publishRedisEvent(channel, payload);
    }

    public void publishOrderbookSummary(String pieceUuid, Map<String, Object> summary) {
        String channel = "piece.orderbook." + pieceUuid;
        try {
            String json = objectMapper.writeValueAsString(summary);
            redisTemplate.convertAndSend(channel, json);
            log.info("[RedisPublisher] OrderbookSummary 발행: channel={}, payload={}", channel, json);
        } catch (JsonProcessingException e) {
            log.error("[RedisPublisher] OrderbookSummary 직렬화 실패", e);
        }
    }

}