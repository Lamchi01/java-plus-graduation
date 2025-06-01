package ru.practicum.ewm.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

@Getter
@Setter
@ToString
@ConfigurationProperties("aggregator.kafka")
public class KafkaConfig {
    private Properties producer;
    private ConsumerProperties consumer;
    private Topic topic;

    @Getter
    @Setter
    @ToString
    public static class ConsumerProperties {
        private Properties base;
        private int poll_duration_seconds;
    }

    @Getter
    @Setter
    @ToString
    public static class Topic {
        private String user_action;
        private String event_similarity;
    }
}