package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.compilation.dto.*;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.storage.CompilationRepository;
import ru.practicum.ewm.event.dto.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.storage.EventRepository;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.storage.RequestRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.ViewStatsDto;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final StatsClient stats;
    private final RequestRepository requestRepository;

    @Override
    public CompilationDto add(NewCompilationDto compilation) {
        List<Event> events = compilation.getEvents() == null ? new ArrayList<>() : eventRepository.findAllById(compilation.getEvents());
        Map<Long, Long> views = getViewsStats(events);
        return compilationMapper.toCompilationDto(compilationRepository.save(
                compilationMapper.toCompilation(compilation, events)),
                events.stream().map(x -> eventMapper.toEventShortDto(x, confirmedRequests(x.getId()),
                        views.get(x.getId()))).toList());
    }

    @Override
    public CompilationDto update(Long id, UpdateCompilationRequest update) {
        Compilation c = compilationRepository.findById(id).orElseThrow(() -> new NotFoundException("Подборка не найдена"));
        if (update.getEvents() != null) {
            c.setEvents(eventRepository.findAllById(update.getEvents()));
        }
        if (update.getPinned() != null) {
            c.setPinned(update.getPinned());
        }
        if (update.getTitle() != null) {
            c.setTitle(update.getTitle());
        }
        Map<Long, Long> views = getViewsStats(c.getEvents());
        return compilationMapper.toCompilationDto(compilationRepository.save(c), c.getEvents().stream()
                .map(x -> eventMapper.toEventShortDto(x, confirmedRequests(x.getId()), views.get(x.getId())))
                .toList());
    }

    @Override
    public List<CompilationDto> getAll(Boolean pinned, Pageable pageable) {
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }
        Map<Long, Long> views = getViewsStats(compilations.stream().map(Compilation::getEvents).flatMap(List::stream).toList());
        return compilations.stream().map(x -> compilationMapper.toCompilationDto(x,
                x.getEvents().stream().map(y -> eventMapper.toEventShortDto(y, confirmedRequests(y.getId()),
                        views.get(y.getId()))).toList())).toList();
    }

    @Override
    public CompilationDto get(Long id) {
        Compilation c = compilationRepository.findById(id).orElseThrow(() -> new NotFoundException("Подборка не найдена"));
        Map<Long, Long> views = getViewsStats(c.getEvents());
        return compilationMapper.toCompilationDto(c, c.getEvents().stream().map(x ->
                eventMapper.toEventShortDto(x, confirmedRequests(x.getId()), views.get(x.getId()))).toList());
    }

    @Override
    public void delete(Long id) {
        compilationRepository.deleteById(id);
    }

    private Map<Long, Long> getViewsStats(List<Event> events) {
        if (events == null || events.isEmpty()) return new HashMap<>();
        List<String> uris = events.stream().map(Event::getId).map(id -> "/events/" + id).toList();
        LocalDateTime start = events.stream()
                .map(event -> event.getPublishedOn() != null ? event.getPublishedOn() : LocalDateTime.now().minusDays(7))
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusDays(7));
        return stats.getStats(start, LocalDateTime.now(), uris, true).stream()
                .collect(Collectors.toMap(x -> Long.parseLong(x.getUri().replace("/events/",
                        "")), ViewStatsDto::getHits));
    }

    private Integer confirmedRequests(Long eventId) {
        return requestRepository.countAllByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }
}
