package ru.practicum.stats.model;

import org.springframework.stereotype.Component;
import ru.practicum.stats.dto.EndpointHitDto;

@Component
public class EndpointHitMapper {
    public EndpointHit toEndpointHit(EndpointHitDto hit) {
        return new EndpointHit(hit.getId(), hit.getApp(), hit.getUri(), hit.getIp(), hit.getTimestamp());
    }

    public EndpointHitDto toEndpointHitDto(EndpointHit hit) {
        return new EndpointHitDto(hit.getId(), hit.getApp(), hit.getUri(), hit.getIp(), hit.getTimestamp());
    }
}
