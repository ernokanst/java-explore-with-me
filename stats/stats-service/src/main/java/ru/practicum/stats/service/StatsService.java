package ru.practicum.stats.service;

import ru.practicum.stats.dto.*;
import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void addHit(EndpointHitDto hit);

    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
