package ru.practicum.ewm.config;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Configuration
@RequiredArgsConstructor
public class KafkaClient {
    private final KafkaConfig kafkaConfig;

    @Bean
    public Producer<Long, SpecificRecordBase> getProducer() {
        return new KafkaProducer<>(kafkaConfig.getProducer());
    }

    @Bean
    public Consumer<Long, UserActionAvro> getConsumer() {
        return new KafkaConsumer<>(kafkaConfig.getConsumer().getBase());
    }
}