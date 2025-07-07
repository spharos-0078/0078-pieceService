package com.pieceofcake.piece_service.trade.infrastructure.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RedisMessageEvent {
    private final String channel;
    private final byte[] body;
}
