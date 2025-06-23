package com.pieceofcake.piece_service.trade.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Component
public class RedisPublisher {

    private final RedisTemplate<String, String> redisTemplate;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 거래량 집계용 데이터 전송
     * @param pieceProductUuid 조각상품 UUID
     * @param piecePrice 체결 가격
     * @param matchedQuantity 체결 수량
     * @param matchedTime 거래 시각 (LocalDateTime)
     */
    public void publishTradeVolume(String pieceProductUuid, long piecePrice, int matchedQuantity, LocalDateTime matchedTime) {
        String date = matchedTime.toLocalDate().toString(); // key에 날짜 기준 사용
        String formattedTime = matchedTime.format(TIME_FORMATTER); // ex. 2025-06-20 14:22:51

        String key = String.format("pieceVolume:%s:%s", pieceProductUuid, date);
        String value = String.format(
                "{\"piecePrice\":%d,\"matchedQuantity\":%d,\"matchedTime\":\"%s\"}",
                piecePrice, matchedQuantity, formattedTime
        );

        redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 호가창 업데이트용 데이터 전송 (수량 누적)
     * @param pieceProductUuid 조각상품 UUID
     * @param price 가격
     * @param quantity 수량
     * @param tradeType BUY 또는 SELL
     */
    public void publishOrderBook(String pieceProductUuid, long price, int quantity, String tradeType) {
        String key = String.format("pieceOrderbook:%s:%s", pieceProductUuid, tradeType.toLowerCase());
        redisTemplate.opsForHash().increment(key, String.valueOf(price), quantity);
    }

}
