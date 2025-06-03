package ru.practicum.ewm.producer;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.config.KafkaProducerProperties;

import java.time.Instant;

@Slf4j
@Component
public class KafkaProducerClient {
    private final Producer<Long, SpecificRecordBase> producer;

    public KafkaProducerClient(KafkaProducerProperties properties) {
        this.producer = new KafkaProducer<>(properties.getProducer());
    }

    public void send(String topic, Instant timestamp, Long eventId, SpecificRecordBase action) {
        ProducerRecord<Long, SpecificRecordBase> record =
                new ProducerRecord<>(
                        topic,
                        null,
                        timestamp.toEpochMilli(),
                        eventId,
                        action);
        log.info("Событие {} отправлено в топик: {}",action, topic);
        producer.send(record);
    }

    @PreDestroy
    public void shutdown() {
        try {
            producer.flush();
            log.info("Выполнена команда flush продюсера");
        } finally {
            producer.close();
            log.info("Выполнена команда close продюсера");
        }
    }
}