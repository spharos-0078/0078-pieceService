package com.pieceofcake.piece_service.trade.infrastructure.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Value("${redis.host}")
    private String redisHost;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        // 단일 노드 Redis (예: localhost:6379)
        config.useSingleServer()
                .setAddress("redis://" + redisHost + ":6379")
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(10)
                .setRetryAttempts(3)
                .setRetryInterval(1500);

        return Redisson.create(config);
    }

}
