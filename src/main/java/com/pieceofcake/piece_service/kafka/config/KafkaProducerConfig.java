package com.pieceofcake.piece_service.kafka.config;

import com.pieceofcake.piece_service.kafka.event.AlertKafkaEvent;
import com.pieceofcake.piece_service.kafka.event.PieceEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServer;

    @Bean
    public Map<String, Object> pieceProducerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return props;
    }

    @Bean
    public ProducerFactory<String, PieceEvent> pieceNotification() {
        return new DefaultKafkaProducerFactory<>(pieceProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, PieceEvent> piecekafkaTemplate() {
        return new KafkaTemplate<>(pieceNotification());
    }

    @Bean
    public Map<String, Object> alertProducerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return props;
    }

    @Bean
    public ProducerFactory<String, AlertKafkaEvent> alertNotification() {
        return new DefaultKafkaProducerFactory<>(alertProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, AlertKafkaEvent> alertkafkaTemplate() {
        return new KafkaTemplate<>(alertNotification());
    }
}
