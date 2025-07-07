package com.pieceofcake.piece_service.trade.application.sse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pieceofcake.piece_service.trade.application.OrderbookSummaryService;
import com.pieceofcake.piece_service.trade.dto.out.MarketPriceSerializeDto;
import com.pieceofcake.piece_service.trade.dto.out.UpdateMarketPriceSseDto;
import com.pieceofcake.piece_service.trade.dto.out.UpdateQuotesSseDto;
import com.pieceofcake.piece_service.trade.infrastructure.redis.RedisMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeSseEventServiceImpl implements TradeSseEventService {
    // sink는 pieceProductUuid별로 관리
    private final Map<String, Sinks.Many<UpdateQuotesSseDto>> quotesSinks = new ConcurrentHashMap<>();
    private final Map<String, Sinks.Many<UpdateMarketPriceSseDto>> matchedSinks = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final OrderbookSummaryService orderbookSummaryService;

    /**
     * SSE 구독 시 Flux 반환 (sink 생성 + 초기 데이터 송출)
     */
    @Override
    public Flux<UpdateQuotesSseDto> getQuotesUpdatesByPieceProductUuid(String pieceProductUuid) {
        return getOrCreateSinkFlux(quotesSinks, pieceProductUuid, this::sendCurrentOrderbook);
    }

    @Override
    public Flux<UpdateMarketPriceSseDto> getMatchedUpdatesByPieceProductUuid(String pieceProductUuid) {
        return getOrCreateSinkFlux(matchedSinks, pieceProductUuid, this::sendCurrentMatched); // 초기데이터 메소드 구현은 선택사항
    }

    @EventListener
    public void handleRedisMessage(RedisMessageEvent event) {
        String channel = event.getChannel();

        final String ORDERBOOK_PREFIX = "piece.orderbook.";
        final String MATCH_PREFIX = "piece.match.";

        try {
            if (channel.startsWith(ORDERBOOK_PREFIX)) {
                String pieceProductUuid = channel.substring(ORDERBOOK_PREFIX.length());
                UpdateQuotesSseDto dto = deserializePayload(event.getBody(), UpdateQuotesSseDto.class);
                emitToSink(quotesSinks, pieceProductUuid, dto, "[TradeSseEventService] 호가정보 SSE 송출");

            } else if (channel.startsWith(MATCH_PREFIX)) {
                String pieceProductUuid = channel.substring(MATCH_PREFIX.length());
                UpdateMarketPriceSseDto dto = deserializePayload(event.getBody(), UpdateMarketPriceSseDto.class);
                emitToSink(matchedSinks, pieceProductUuid, dto, "[TradeSseEventService] 체결정보 SSE 송출");

            } else {
                log.warn("[TradeSseEventService] 처리하지 않는 채널 수신: channel={}", channel);
            }
        } catch (Exception e) {
            log.error("[TradeSseEventService] 메시지 처리 실패: channel={}", channel, e);
        }
    }

    /**
     * Redis pub된 JSON 바이트 데이터를 DTO로 역직렬화
     */
    private <T> T deserializePayload(byte[] body, Class<T> clazz) throws IOException {
        return objectMapper.readValue(body, clazz);
    }

    /**
     * sink에 이벤트를 emit하고 로그를 남김
     */
    private <T> void emitToSink(Map<String, Sinks.Many<T>> sinks, String pieceProductUuid, T event, String logPrefix) {
        Sinks.Many<T> sink = sinks.get(pieceProductUuid);
        if (sink != null) {
            sink.tryEmitNext(event);
            log.info("{}: pieceProductUuid={}, event={}", logPrefix, pieceProductUuid, event);
        } else {
            log.warn("[RedisSubscriber] sink 없음: pieceProductUuid={}, eventType={}", pieceProductUuid, event.getClass().getSimpleName());
        }
    }


    /**
     * sink를 생성/조회하고, 최초 데이터 송출 후 Flux 반환
     */
    private <T> Flux<T> getOrCreateSinkFlux(Map<String, Sinks.Many<T>> sinkMap,
                                            String pieceProductUuid,
                                            BiConsumer<String, Sinks.Many<T>> sendCurrentData) {
        Sinks.Many<T> sink = sinkMap.computeIfAbsent(pieceProductUuid,
                k -> Sinks.many().multicast().onBackpressureBuffer());

        sendCurrentData.accept(pieceProductUuid, sink);
        return sink.asFlux();
    }

    /**
     * 최초 접속 시 Redis에 저장된 최신 호가정보 송출
     */
    private void sendCurrentOrderbook(String pieceProductUuid, Sinks.Many<UpdateQuotesSseDto> sink) {
        try {
            // 🔥 Redis에서 조회하는 대신 buildOrderbookSummary()를 직접 호출
            Map<String, Object> summaryData = orderbookSummaryService.buildOrderbookSummary(pieceProductUuid);

            UpdateQuotesSseDto event = UpdateQuotesSseDto.builder()
                    .askp(objectMapper.convertValue(summaryData.get("askp"), new TypeReference<List<Long>>() {
                    }))
                    .bidp(objectMapper.convertValue(summaryData.get("bidp"), new TypeReference<List<Long>>() {
                    }))
                    .askpRsqn(objectMapper.convertValue(summaryData.get("askpRsqn"), new TypeReference<List<Long>>() {
                    }))
                    .bidRsqn(objectMapper.convertValue(summaryData.get("bidRsqn"), new TypeReference<List<Long>>() {
                    }))
                    .build();


//            sink.tryEmitNext(event);
            Sinks.EmitResult result = sink.tryEmitNext(event);
            if (result.isFailure()) {
                log.warn("[RedisSubscriber] 초기 호가정보 emit 실패: pieceProductUuid={}, result={}", pieceProductUuid, result);
            }
            log.info("[RedisSubscriber] 초기 호가정보 송출(build): pieceProductUuid={}, event={}", pieceProductUuid, event);

        } catch (Exception e) {
            log.error("[RedisSubscriber] 초기 호가정보 생성 실패(build): pieceProductUuid={}", pieceProductUuid, e);
        }
    }

    private void sendCurrentMatched(String pieceProductUuid, Sinks.Many<UpdateMarketPriceSseDto> sink) {
        // 선택: 최근 체결 데이터 조회 후 sink로 emit
        // 예: 최근 체결 1건 Redis에서 꺼내기 (저장 구조에 맞춰 키/타입 수정 필요)
        try {
            String date = LocalDate.now().toString();
            String key = String.format("pieceVolume:%s:%s", pieceProductUuid, date);
            String latestTradeJson = stringRedisTemplate.opsForList().index(key, 0);

            if (latestTradeJson != null) {
                MarketPriceSerializeDto dto = objectMapper.readValue(latestTradeJson, MarketPriceSerializeDto.class);
                UpdateMarketPriceSseDto event = UpdateMarketPriceSseDto.builder().marketPrice(dto.getPiecePrice()).build();
                sink.tryEmitNext(event);
                log.info("[RedisSubscriber] 초기 체결정보 송출: pieceProductUuid={}, event={}", pieceProductUuid, event);
            } else {
                log.info("[RedisSubscriber] 초기 체결정보 없음: pieceProductUuid={}", pieceProductUuid);
            }
        } catch (Exception e) {
            log.error("[RedisSubscriber] 초기 체결정보 조회 실패: pieceProductUuid={}", pieceProductUuid, e);
        }
    }

    /**
     * Redis에서 JSON 배열 형태의 문자열을 List<Long>으로 변환
     */
    private List<Long> convertStringToLongList(String jsonArrayStr) throws IOException {
        if (jsonArrayStr == null) return Collections.emptyList();
        return objectMapper.readValue(jsonArrayStr, new TypeReference<List<Long>>() {
        });
    }
}
