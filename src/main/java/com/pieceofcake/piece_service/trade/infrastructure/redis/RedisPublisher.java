package com.pieceofcake.piece_service.trade.infrastructure.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
     * 호가창 업데이트 및 로그 저장
     *
     * Key (Hash): pieceOrderbook:{조각상품UUID}:{buy|sell} → price : quantity (누적)
     * Key (List): pieceOrderLog:{조각상품UUID}:{buy|sell} → {pieceProductUuid, price, quantity, tradeType, timestamp}
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
}