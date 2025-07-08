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
import reactor.core.Scannable.Attr;

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
                MarketPriceSerializeDto serializeDto = deserializePayload(event.getBody(), MarketPriceSerializeDto.class);
                System.out.println(serializeDto.getPiecePrice());
                UpdateMarketPriceSseDto dto = UpdateMarketPriceSseDto.builder().marketPrice(serializeDto.getPiecePrice()).build();
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

    private <T> Flux<T> getOrCreateSinkFlux(Map<String, Sinks.Many<T>> sinkMap,
                                            String pieceProductUuid,
                                            BiConsumer<String, Sinks.Many<T>> sendCurrentData) {
        Sinks.Many<T> sink = sinkMap.compute(pieceProductUuid, (k, existingSink) -> {
            if (existingSink == null || Boolean.TRUE.equals(existingSink.scan(Attr.TERMINATED))) {
                log.info("[SSE] 새로운 sink 생성: pieceProductUuid={}", pieceProductUuid);
                return Sinks.many().replay().latest();  // ✅ replay.latest로 변경
            }
            return existingSink;
        });

        log.info("[SSE] sink 상태: pieceProductUuid={}, subscribers={}, terminated={}",
                pieceProductUuid, sink.currentSubscriberCount(), sink.scan(Attr.TERMINATED));

        Flux<T> flux = sink.asFlux()
                .doOnSubscribe(sub ->
                        log.info("[SSE] 구독 시작: pieceProductUuid={}", pieceProductUuid))
                .doOnCancel(() -> {
                    log.info("[SSE] 구독 취소: pieceProductUuid={}", pieceProductUuid);
                    cleanupSinkIfNoSubscribers(sinkMap, pieceProductUuid, sink);
                })
                .doOnComplete(() -> {
                    log.info("[SSE] 구독 정상 종료: pieceProductUuid={}", pieceProductUuid);
                    cleanupSinkIfNoSubscribers(sinkMap, pieceProductUuid, sink);
                })
                .doOnError(e -> {
                    if (e instanceof IOException) {
                        log.info("[SSE] 클라이언트 연결 종료 감지 (무시): pieceProductUuid={}", pieceProductUuid);
                    } else {
                        log.error("[SSE] 구독 중 에러 발생: pieceProductUuid={}", pieceProductUuid, e);
                    }
                    cleanupSinkIfNoSubscribers(sinkMap, pieceProductUuid, sink);
                });

        // sink에 초기 데이터 송출
        sendCurrentData.accept(pieceProductUuid, sink);

        return flux;
    }

    /**
     * 구독자 없으면 sink를 제거
     */
    private <T> void cleanupSinkIfNoSubscribers(Map<String, Sinks.Many<T>> sinkMap,
                                                String pieceProductUuid,
                                                Sinks.Many<T> sink) {
        int subscribers = sink.currentSubscriberCount();
        boolean terminated = Boolean.TRUE.equals(sink.scan(Attr.TERMINATED));

        log.info("[SSE] sink 상태 확인: pieceProductUuid={}, subscribers={}, terminated={}",
                pieceProductUuid, subscribers, terminated);

        if (subscribers == 0) {
            sinkMap.remove(pieceProductUuid, sink);
            log.info("[SSE] sink 제거 완료: pieceProductUuid={}", pieceProductUuid);
        }
    }

    /**
     * sink에 emit 시도 후 실패 여부를 로그로 남김
     */
    private <T> void emitToSink(Map<String, Sinks.Many<T>> sinks, String pieceProductUuid, T event, String logPrefix) {
        Sinks.Many<T> sink = sinks.get(pieceProductUuid);
        if (sink != null) {
            Sinks.EmitResult result = sink.tryEmitNext(event);
            if (result.isSuccess()) {
                log.info("{}: pieceProductUuid={}, event={}", logPrefix, pieceProductUuid, event);
            } else {
                log.warn("{} emit 실패: pieceProductUuid={}, result={}", logPrefix, pieceProductUuid, result);
            }
        } else {
            log.warn("[SSE] sink 없음: pieceProductUuid={}, eventType={}", pieceProductUuid, event.getClass().getSimpleName());
        }
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
