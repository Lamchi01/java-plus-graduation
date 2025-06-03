package ru.practicum.ewm.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "collector.kafka")
public class KafkaProducerProperties {
    private Properties producer;
    private Topic topic;

    @Getter
    @Setter
    @ToString
    public static class Topic {
        private String userAction;
    }
}