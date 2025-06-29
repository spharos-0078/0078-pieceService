package com.pieceofcake.piece_service.trade.application;

import com.pieceofcake.piece_service.common.entity.BaseResponseStatus;
import com.pieceofcake.piece_service.common.exception.BaseException;
import com.pieceofcake.piece_service.piece.infrastructure.PieceProductRepository;
import com.pieceofcake.piece_service.trade.util.PriceStepValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderbookSummaryService {
    private final RedisTemplate<String, String> redisTemplate;
    private final PriceStepValidator priceStepValidator;
    private final PieceProductRepository pieceProductRepository;

    public Map<String, Object> buildOrderbookSummary(String pieceUuid) {
        // 1) 체결가 조회
        String priceKey = "pieceLastPrice:" + pieceUuid;
        String lastPriceStr = redisTemplate.opsForValue().get(priceKey);
        if (lastPriceStr == null) {
            lastPriceStr = pieceProductRepository.findByPieceProductUuid(pieceUuid).orElseThrow(
                    () -> new BaseException(BaseResponseStatus.NO_EXIST_PIECE_PRODUCT)
            ).getMarketPrice().toString();
//            throw new IllegalStateException("체결가 없음");
        }

        long lastPrice = Long.parseLong(lastPriceStr);
        long step = priceStepValidator.getPriceStep(lastPrice);

        List<Long> askp = new ArrayList<>();
        List<Long> bidp = new ArrayList<>();
        List<Long> askpRsqn = new ArrayList<>();
        List<Long> bidRsqn = new ArrayList<>();

        // 2) 매도 호가 ask: 체결가 기준 위로 10개
        for (int i = 1; i <= 10; i++) {
            long price = lastPrice + step * i;
            askp.add(price);
            askpRsqn.add(getOrderbookQuantity(pieceUuid, price, "SELL"));
        }

        // 3) 매수 호가 bid: 체결가 기준 아래로 10개
        for (int i = 1; i <= 10; i++) {
            long price = lastPrice - step * i;
            bidp.add(price);
            bidRsqn.add(getOrderbookQuantity(pieceUuid, price, "BUY"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("askp", askp);
        response.put("bidp", bidp);
        response.put("askpRsqn", askpRsqn);
        response.put("bidRsqn", bidRsqn);

        return response;
    }

    private long getOrderbookQuantity(String pieceUuid, long price, String type) {
        String key = String.format("pieceOrderbook:%s:%s", pieceUuid, type);
        String qtyStr = (String) redisTemplate.opsForHash().get(key, String.valueOf(price));
        return qtyStr != null ? Long.parseLong(qtyStr) : 0L;
    }
}
