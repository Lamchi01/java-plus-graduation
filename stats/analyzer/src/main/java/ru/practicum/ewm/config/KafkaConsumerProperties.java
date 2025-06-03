package ru.practicum.ewm.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "analyzer")
public class KafkaConsumerProperties {
    private Kafka kafka;

    @Getter
    @Setter
    @ToString
    public static class Kafka {
        private Consumer consumer;
        private Topic topic;
    }

    @Getter
    @Setter
    @ToString
    public static class Consumer {
        private Properties user_action;
        private Properties event_similarity;
        private PollDuration poll_duration;
    }

    @Getter
    @Setter
    @ToString
    public static class Topic {
        private String user_action;
        private String event_similarity;
    }

    @Getter
    @Setter
    @ToString
    public static class PollDuration {
        private Long user_action;
        private Long event_similarity;
    }
}