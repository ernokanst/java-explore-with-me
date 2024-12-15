package ru.practicum.ewm.compilation.service;

import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.ExploreWithMeServer;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.storage.CommentRepository;
import ru.practicum.ewm.compilation.dto.*;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.storage.CompilationRepository;
import ru.practicum.ewm.event.dto.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.storage.EventRepository;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.ViewStatsDto;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;
    private final EventMapper eventMapper;
    private final StatsClient stats;
    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public CompilationDto add(NewCompilationDto compilation) {
        List<Event> events = compilation.getEvents() == null ? new ArrayList<>() : eventRepository.findAllById(compilation.getEvents());
        Map<Long, Long> views = getViewsStats(events);
        Map<Long, Integer> confirmed = getConfirmed();
        Map<Long, List<Comment>> comments = getComments(events);
        return compilationMapper.toCompilationDto(compilationRepository.save(
                compilationMapper.toCompilation(compilation, events)),
                events.stream().map(x -> eventMapper.toEventShortDto(x, confirmed.get(x.getId()),
                        views.get(x.getId()), comments.get(x.getId()))).toList());
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
        Map<Long, Integer> confirmed = getConfirmed();
        Map<Long, List<Comment>> comments = getComments(c.getEvents());
        return compilationMapper.toCompilationDto(compilationRepository.save(c), c.getEvents().stream()
                .map(x -> eventMapper.toEventShortDto(x, confirmed.get(x.getId()), views.get(x.getId()),
                        comments.get(x.getId()))).toList());
    }

    @Override
    public List<CompilationDto> getAll(Boolean pinned, Pageable pageable) {
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }
        List<Event> events = compilations.stream().map(Compilation::getEvents).flatMap(List::stream).toList();
        Map<Long, Long> views = getViewsStats(events);
        Map<Long, Integer> confirmed = getConfirmed();
        Map<Long, List<Comment>> comments = getComments(events);
        return compilations.stream().map(x -> compilationMapper.toCompilationDto(x,
                x.getEvents().stream().map(y -> eventMapper.toEventShortDto(y, confirmed.get(y.getId()),
                        views.get(y.getId()), comments.get(y.getId()))).toList())).toList();
    }

    @Override
    public CompilationDto get(Long id) {
        Compilation c = compilationRepository.findById(id).orElseThrow(() -> new NotFoundException("Подборка не найдена"));
        Map<Long, Long> views = getViewsStats(c.getEvents());
        Map<Long, Integer> confirmed = getConfirmed();
        Map<Long, List<Comment>> comments = getComments(c.getEvents());
        return compilationMapper.toCompilationDto(c, c.getEvents().stream().map(x ->
                eventMapper.toEventShortDto(x, confirmed.get(x.getId()), views.get(x.getId()), comments.get(x.getId())))
                .toList());
    }

    @Override
    public void delete(Long id) {
        compilationRepository.deleteById(id);
    }

    private Map<Long, Long> getViewsStats(List<Event> events) {
        if (events == null || events.isEmpty()) return new HashMap<>();
        List<String> uris = events.stream().map(Event::getId).map(id -> "/events/" + id).toList();
        LocalDateTime start = events.stream()
                .map(event -> event.getPublishedOn() != null ? event.getPublishedOn() :
                        LocalDateTime.now().minusDays(ExploreWithMeServer.DEFAULT_STATS_INTERVAL))
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusDays(ExploreWithMeServer.DEFAULT_STATS_INTERVAL));
        return stats.getStats(start, LocalDateTime.now(), uris, true).stream()
                .collect(Collectors.toMap(x -> Long.parseLong(x.getUri().replace("/events/",
                        "")), ViewStatsDto::getHits));
    }

    private Map<Long, Integer> getConfirmed() {
        return entityManager.createQuery("SELECT r.event.id AS id, COUNT(r) AS count FROM Request r " +
                        "WHERE r.status = 'CONFIRMED' GROUP BY r.event.id", Tuple.class).getResultStream()
                .collect(Collectors.toMap(tuple -> ((Number) tuple.get("id")).longValue(),
                        tuple -> ((Number) tuple.get("count")).intValue()));
    }

    private Map<Long, List<Comment>> getComments(List<Event> events) {
        List<Comment> comments = commentRepository.findByEventIdIn(events.stream().map(Event::getId).toList());
        Map<Long, List<Comment>> result = new HashMap<>();
        for (Comment c : comments) {
            if (!(result.containsKey(c.getEvent().getId()))) {
                result.put(c.getEvent().getId(), new ArrayList<>());
            }
            result.get(c.getEvent().getId()).add(c);
        }
        return result;
    }
}
