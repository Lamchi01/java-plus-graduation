package ru.yandex.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.yandex.practicum.ParamDto;
import ru.yandex.practicum.ParamHitDto;
import ru.yandex.practicum.ViewStats;

import java.util.List;

@Slf4j
@Component
public class RestStatClient implements StatClient {
    private final DiscoveryClient discoveryClient;
    private RestClient restClient;

    @Autowired
    public RestStatClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    public void hit(ParamHitDto paramHitDto) {
        try {
            this.restClient = RestClient.create(getInstance().getUri().toString());
            restClient.post()
                    .uri("/hit")
                    .body(paramHitDto)
                    .retrieve()
                    .body(ViewStats.class);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    @Override
    public List<ViewStats> getStat(ParamDto paramDto) {
        try {
            this.restClient = RestClient.create(getInstance().getUri().toString());
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/stats")
                            .queryParam("start", paramDto.getStart().toString())
                            .queryParam("end", paramDto.getEnd().toString())
                            .queryParam("uris", paramDto.getUris())
                            .queryParam("unique", paramDto.getUnique())
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ViewStats>>() {
                    });
        } catch (Exception e) {
            log.info(e.getMessage());
            return List.of();
        }
    }

    private ServiceInstance getInstance() {
        try {
            return discoveryClient
                    .getInstances("stats-server")
                    .getFirst();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Ошибка обнаружения адреса сервиса статистики с id: " + "stats-server", e
            );
        }
    }
}