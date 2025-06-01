package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalyzerStart implements CommandLineRunner {
    private final UserActionProcessor userActionProcessor;
    private final EventSimilarityProcessor eventSimilarityProcessor;

    @Override
    public void run(String... args) throws Exception {
        Thread userActionThread = new Thread(userActionProcessor);
        userActionThread.setName("UserActionProcessor");
        userActionThread.start();

        eventSimilarityProcessor.run();
    }
}