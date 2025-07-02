package com.pieceofcake.piece_service.trade.application.domain;

import com.pieceofcake.piece_service.trade.dto.in.CreateMatchedHistoryRequestDto;
import com.pieceofcake.piece_service.trade.dto.in.CreateTradedHistoryRequestDto;
import com.pieceofcake.piece_service.trade.entity.OwnedPiece;
import com.pieceofcake.piece_service.trade.entity.PieceTradeReservation;
import com.pieceofcake.piece_service.trade.entity.TradeType;
import com.pieceofcake.piece_service.trade.infrastructure.PieceMatchedHistoryRepository;
import com.pieceofcake.piece_service.trade.infrastructure.TradedHistoryRepository;
import com.pieceofcake.piece_service.trade.infrastructure.redis.RedisPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeHistoryService {
    
    private final TradedHistoryRepository tradedHistoryRepository;
    private final PieceMatchedHistoryRepository pieceMatchedHistoryRepository;
    private final RedisPublisher redisPublisher;
    
    /**
     * 체결 이력 저장
     */
    @Transactional
    public void saveTradeHistory(PieceTradeReservation buy, PieceTradeReservation sell, 
                                List<OwnedPiece> pieces, String matchedUuid) {
        
        for (OwnedPiece piece : pieces) {
            // 매수 이력 저장
            tradedHistoryRepository.save(CreateTradedHistoryRequestDto.of(
                    buy, piece.getPieceUuid(), TradeType.BUY, buy.getMemberUuid()).toEntity());
            
            // 매도 이력 저장
            tradedHistoryRepository.save(CreateTradedHistoryRequestDto.of(
                    sell, piece.getPieceUuid(), TradeType.SELL, sell.getMemberUuid()).toEntity());
        }
        
        log.info("거래 이력 저장 완료: matchedUuid={}, pieceCount={}", matchedUuid, pieces.size());
    }
    
    /**
     * 매칭 이력 저장 및 이벤트 발행
     */
    @Transactional
    public void saveMatchedHistory(PieceTradeReservation reservation, PieceTradeReservation counter,
                                  String matchedUuid, long piecePrice, int matchedQuantity, 
                                  LocalDateTime matchedTime) {
        
        PieceTradeReservation buyReservation = reservation.getTradeType() == TradeType.BUY ? reservation : counter;
        PieceTradeReservation sellReservation = reservation.getTradeType() == TradeType.SELL ? reservation : counter;
        
        // 매수 이력 저장
        pieceMatchedHistoryRepository.save(CreateMatchedHistoryRequestDto.of(
                buyReservation, matchedUuid, piecePrice, matchedQuantity, 
                buyReservation.getMemberUuid(), TradeType.BUY).toEntity());
        
        // 매도 이력 저장
        pieceMatchedHistoryRepository.save(CreateMatchedHistoryRequestDto.of(
                sellReservation, matchedUuid, piecePrice, matchedQuantity, 
                sellReservation.getMemberUuid(), TradeType.SELL).toEntity());
        
        // Redis 이벤트 발행
        publishTradeEvents(reservation.getPieceProductUuid(), piecePrice, matchedQuantity, matchedTime);
        
        log.info("매칭 이력 저장 완료: matchedUuid={}, price={}, quantity={}", 
                matchedUuid, piecePrice, matchedQuantity);
    }
    
    private void publishTradeEvents(String pieceProductUuid, long piecePrice, 
                                   int matchedQuantity, LocalDateTime matchedTime) {
        
        // 거래량 이벤트 발행
        redisPublisher.publishTradeVolume(pieceProductUuid, piecePrice, matchedQuantity, matchedTime);
        
        // Redis Pub/Sub 이벤트 발행
        Map<String, Object> pubsubPayload = new HashMap<>();
        pubsubPayload.put("pieceProductUuid", pieceProductUuid);
        pubsubPayload.put("piecePrice", piecePrice);
        pubsubPayload.put("matchedQuantity", matchedQuantity);
        pubsubPayload.put("matchedTime", matchedTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        redisPublisher.publishRedisEvent("trade-matched", pubsubPayload);
    }
} 