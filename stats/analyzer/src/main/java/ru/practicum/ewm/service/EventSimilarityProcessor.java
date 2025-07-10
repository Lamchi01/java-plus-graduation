package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.config.KafkaConsumerProperties;
import ru.practicum.ewm.service.handler.EventSimilarityHandler;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSimilarityProcessor implements Runnable {
    private final Consumer<Long, EventSimilarityAvro> consumer;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffset = new HashMap<>();
    private final EventSimilarityHandler eventSimilarityHandler;
    private final KafkaConsumerProperties kafkaConsumerProperties;

    @Override
    public void run() {
        try {
            consumer.subscribe(List.of(kafkaConsumerProperties.getKafka().getTopic().getEvent_similarity()));
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            while (true) {
                ConsumerRecords<Long, EventSimilarityAvro> records = consumer.poll(
                        kafkaConsumerProperties
                                .getKafka()
                                .getConsumer()
                                .getPoll_duration()
                                .getEvent_similarity()
                );
                if (!records.isEmpty()) {
                    log.info("Обработка {} событий", records.count());
                }
                for (ConsumerRecord<Long, EventSimilarityAvro> record : records) {
                    eventSimilarityHandler.handleEventSimilarity(record.value());
                    currentOffset.put(
                            new TopicPartition(record.topic(), record.partition()),
                            new OffsetAndMetadata(record.offset() + 1)
                    );
                }
                consumer.commitAsync((offsets, exception) -> {
                    if (exception != null) {
                        log.error("Ошибка при фиксации оффсета", exception);
                    }
                });
            }
        } catch (WakeupException ignore) {
        } catch (Exception e) {
            log.error("При обработке возникла ошибка", e);
        } finally {
            try {
                consumer.commitSync(currentOffset);
            } finally {
                log.info("Закрываем consumer EventSimilarity");
                consumer.close();
                log.info("Consumer EventSimilarity закрыт");
            }
        }
    }
}