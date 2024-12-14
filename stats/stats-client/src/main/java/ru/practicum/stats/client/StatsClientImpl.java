package ru.practicum.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.*;
import java.time.LocalDateTime;
import java.util.*;


@Slf4j
@Service
public class StatsClientImpl implements StatsClient {
    private final RestTemplate rest;
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    public StatsClientImpl(@Value("${stats-service.url}") String serverUrl, RestTemplateBuilder builder) {
        rest = builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                      .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                      .build();
    }

    @Override
    public void addHit(EndpointHitDto hit) {
        rest.postForObject("/hit", hit, ResponseEntity.class);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String uri = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", String.join(",", uris))
                .queryParam("unique", unique)
                .toUriString();
        ResponseEntity<List<ViewStatsDto>> response = rest.exchange(uri, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});
        return response.getBody();
    }
}
