package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.exceptions.ValidationException;
import ru.practicum.stats.model.EndpointHitMapper;
import ru.practicum.stats.storage.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;
    private final EndpointHitMapper hitMapper;

    @Override
    public void addHit(EndpointHitDto hit) {
        statsRepository.save(hitMapper.toEndpointHit(hit));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end) || start.isAfter(LocalDateTime.now())) {
            throw new ValidationException("Диапазон статистики указан некорректно");
        }
        if (unique) {
            if (uris != null && !uris.isEmpty()) {
                return statsRepository.getUniqueStatsForUris(start, end, uris);
            } else {
                return statsRepository.getUniqueStats(start, end);
            }
        } else {
            if (uris != null && !uris.isEmpty()) {
                return statsRepository.getStatsForUris(start, end, uris);
            } else {
                return statsRepository.getStats(start, end);
            }
        }
    }
}
