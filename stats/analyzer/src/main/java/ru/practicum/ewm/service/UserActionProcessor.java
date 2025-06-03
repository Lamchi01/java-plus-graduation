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
import ru.practicum.ewm.service.handler.UserActionHandler;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionProcessor implements Runnable {
    private final Consumer<Long, UserActionAvro> consumer;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffset = new HashMap<>();
    private final UserActionHandler userActionHandler;
    private final KafkaConsumerProperties kafkaConsumerProperties;

    @Override
    public void run() {
        try {
            consumer.subscribe(List.of(kafkaConsumerProperties.getKafka().getTopic().getUser_action()));
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            while (true) {
                ConsumerRecords<Long, UserActionAvro> records = consumer.poll(
                        kafkaConsumerProperties
                                .getKafka()
                                .getConsumer()
                                .getPoll_duration()
                                .getUser_action()
                );
                if (!records.isEmpty()) {
                    log.info("Обработка {} событий", records.count());
                }
                for (ConsumerRecord<Long, UserActionAvro> record : records) {
                    userActionHandler.handleUserAction(record.value());
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
                log.info("Закрываем consumer UserAction");
                consumer.close();
                log.info("Consumer UserAction закрыт");
            }
        }
    }
}