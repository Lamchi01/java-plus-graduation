package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.config.KafkaConfig;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregatorServiceImpl implements CommandLineRunner {
    private final Consumer<Long, UserActionAvro> consumer;
    private final Producer<Long, SpecificRecordBase> producer;
    private final Map<Long, Map<Long, Double>> userActionsWeightMap = new HashMap<>();
    private final Map<Long, Double> weightSumMap = new HashMap<>();
    private final Map<Long, Map<Long, Double>> minWeightSumMap = new HashMap<>();
    private final Map<TopicPartition, OffsetAndMetadata> currentOffset = new HashMap<>();
    private final KafkaConfig kafkaConfig;

    @Override
    public void run(String... args) throws Exception {
        try {
            consumer.subscribe(List.of(kafkaConfig.getTopic().getUser_action()));
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            while (true) {
                ConsumerRecords<Long, UserActionAvro> records =
                        consumer.poll(kafkaConfig.getConsumer().getPoll_duration_seconds());
                for (ConsumerRecord<Long, UserActionAvro> record : records) {
                    UserActionAvro userActionAvro = record.value();
                    long eventId = userActionAvro.getEventId();
                    if (!userActionsWeightMap.containsKey(eventId)) {
                        calculateSimilarityForNewEvent(userActionAvro).forEach(this::send);
                    } else {
                        calculateSimilarityForExistingEvent(userActionAvro).forEach(this::send);
                    }
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
            log.error("Ошибка при обработке событий", e);
        } finally {
            try {
                consumer.commitSync(currentOffset);
                producer.flush();
            } finally {
                log.info("Закрытие продюсера и консьюмера");
                producer.close();
                log.info("Продюсер закрыт");
                consumer.close();
                log.info("Консьюмер закрыт");
            }
        }
    }

    public void send(EventSimilarityAvro eventSimilarityAvro) {
        log.info("Процесс отправки события {} в топик {}", eventSimilarityAvro, kafkaConfig.getTopic().getEvent_similarity());
        ProducerRecord<Long, SpecificRecordBase> record = new ProducerRecord<>(
                kafkaConfig.getTopic().getEvent_similarity(),
                null,
                eventSimilarityAvro.getTimestamp().toEpochMilli(),
                eventSimilarityAvro.getEventA(),
                eventSimilarityAvro
        );
        producer.send(record);
        log.info("Событие {} отправлено в топик {}", eventSimilarityAvro, kafkaConfig.getTopic().getEvent_similarity());
    }

    private List<EventSimilarityAvro> calculateSimilarityForNewEvent(UserActionAvro userActionAvro) {
        long userId = userActionAvro.getUserId();
        long eventId = userActionAvro.getEventId();
        double weight = getWeightAction(userActionAvro);
        userActionsWeightMap.computeIfAbsent(eventId, e -> new HashMap<>()).put(userId, weight);
        List<EventSimilarityAvro> similarityAvroList = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : weightSumMap.entrySet()) {
            long first = Math.min(eventId, entry.getKey());
            long second = Math.max(eventId, entry.getKey());
            double minWeightsSum = Math.min(userActionsWeightMap.get(entry.getKey()).getOrDefault(userId, 0.0), weight);
            Map<Long, Double> minWeightSumMapForFirstEvent = minWeightSumMap.computeIfAbsent(first, e -> new HashMap<>());
            if (minWeightsSum != 0.0) {
                minWeightSumMapForFirstEvent.put(second, minWeightsSum);
                double score = minWeightsSum / (Math.sqrt(entry.getValue()) * Math.sqrt(weight));
                similarityAvroList.add(EventSimilarityAvro.newBuilder()
                        .setEventA(first)
                        .setEventB(second)
                        .setScore(score)
                        .setTimestamp(userActionAvro.getTimestamp())
                        .build());
            }
        }
        weightSumMap.put(eventId, weight);
        return similarityAvroList;
    }

    private List<EventSimilarityAvro> calculateSimilarityForExistingEvent(UserActionAvro userActionAvro) {
        long eventA = userActionAvro.getEventId();
        Map<Long, Double> usersWeight = userActionsWeightMap.get(eventA);
        long userId = userActionAvro.getUserId();
        double oldWeight = usersWeight.getOrDefault(userId, 0.0);
        double newWeight = getWeightAction(userActionAvro);
        if (oldWeight >= newWeight) {
            return List.of();
        }
        usersWeight.put(userId, newWeight);
        weightSumMap.computeIfPresent(eventA, (key, oldValue) -> oldValue + (newWeight - oldWeight));
        List<EventSimilarityAvro> similarityAvroList = new ArrayList<>();
        for (Long eventB : userActionsWeightMap.keySet()) {
            if (eventA == eventB) continue;
            double eventBUserWeight = userActionsWeightMap.get(eventB).getOrDefault(userId, 0.0);
            if (eventBUserWeight == 0.0) continue;
            long first = Math.min(eventA, eventB);
            long second = Math.max(eventA, eventB);
            double delta = Math.min(newWeight, eventBUserWeight) - Math.min(oldWeight, eventBUserWeight);
            Map<Long, Double> minWeightSumMapForFirstEvent = minWeightSumMap.get(first);
            minWeightSumMapForFirstEvent.put(second, minWeightSumMapForFirstEvent.getOrDefault(second, 0.0) + delta);
            double score = minWeightSumMapForFirstEvent.get(second) / (Math.sqrt(weightSumMap.get(first)) * Math.sqrt(weightSumMap.get(second)));
            similarityAvroList.add(EventSimilarityAvro.newBuilder()
                    .setEventA(first)
                    .setEventB(second)
                    .setScore(score)
                    .setTimestamp(userActionAvro.getTimestamp())
                    .build());
        }
        return similarityAvroList;
    }

    private Double getWeightAction(UserActionAvro userActionAvro) {
        return switch (userActionAvro.getActionType()) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}