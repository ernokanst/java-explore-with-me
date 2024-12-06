package ru.practicum.stats.client;

import ru.practicum.stats.dto.*;
import java.time.LocalDateTime;
import java.util.List;

public interface StatsClient {
    void addHit(EndpointHitDto hit);

    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
