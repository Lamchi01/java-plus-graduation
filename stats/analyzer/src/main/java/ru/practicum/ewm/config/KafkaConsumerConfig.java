package ru.practicum.ewm.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {
    private final KafkaConsumerProperties kafkaConsumerProperties;

    @Bean
    public Consumer<Long, UserActionAvro> getConsumerAction() {
        return new KafkaConsumer<>(kafkaConsumerProperties.getKafka().getConsumer().getUser_action());
    }

    @Bean
    public Consumer<Long, EventSimilarityAvro> getConsumerSimilarity() {
        return new KafkaConsumer<>(kafkaConsumerProperties.getKafka().getConsumer().getUser_action());
    }
}