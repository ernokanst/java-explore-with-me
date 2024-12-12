package ru.practicum.ewm.event.service;

import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.ExploreWithMeServer;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.storage.CategoryRepository;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.*;
import ru.practicum.ewm.event.storage.EventRepository;
import ru.practicum.ewm.exceptions.*;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.storage.UserRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.ViewStatsDto;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient stats;
    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public EventFullDto add(Long userId, NewEventDto event) {
        User initiator = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Category category = categoryRepository.findById(event.getCategory()).orElseThrow(() -> new NotFoundException("Категория не найдена"));
        Event e = eventMapper.toEvent(event, initiator, category);
        return eventMapper.toEventFullDto(eventRepository.save(e), 0, 0L);
    }

    @Override
    public EventFullDto updateAdmin(Long eventId, UpdateEventAdminRequest event) {
        Event e = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (e.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Событие уже опубликовано");
        }
        if (e.getState().equals(EventState.CANCELED)) {
            throw new ConflictException("Событие отменено");
        }
        Map<Long, Integer> confirmed = getConfirmed();
        return eventMapper.toEventFullDto(eventRepository.save(eventMapper.toAdminUpdatedEvent(e, event)),
                confirmed.get(eventId) != null ? confirmed.get(eventId) : 0, getViewsStats(List.of(e)).get(eventId));
    }

    @Override
    public EventFullDto updateUser(Long userId, Long eventId, UpdateEventUserRequest event) {
        Event e = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (!e.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Пользователь не соответствует создателю события");
        }
        if (e.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Событие уже опубликовано");
        }
        Map<Long, Integer> confirmed = getConfirmed();
        return eventMapper.toEventFullDto(eventRepository.save(eventMapper.toUserUpdatedEvent(e, event)),
                confirmed.get(eventId) != null ? confirmed.get(eventId) : 0, getViewsStats(List.of(e)).get(eventId));
    }

    @Override
    public List<EventFullDto> getAllAdmin(List<Long> users, List<String> states, List<Long> categories,
                                          LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable) {
        if (users == null || users.isEmpty() || users.getFirst() == 0) {
            users = userRepository.findAll().stream().map(User::getId).toList();
        }
        if (states == null || states.isEmpty() || states.getFirst().isBlank()) {
            states = List.of("PENDING", "PUBLISHED", "CANCELED");
        }
        if (categories == null || categories.isEmpty() || categories.getFirst() == 0) {
            categories = categoryRepository.findAll().stream().map(Category::getId).toList();
        }
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusDays(ExploreWithMeServer.DEFAULT_STATS_INTERVAL);
        }
        List<Event> events = eventRepository.findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateBetween(users,
                states, categories, rangeStart, rangeEnd, pageable);
        Map<Long, Long> views = getViewsStats(events);
        Map<Long, Integer> confirmed = getConfirmed();
        return events.stream().map(x -> eventMapper.toEventFullDto(x,
                confirmed.get(x.getId()) != null ? confirmed.get(x.getId()) : 0, views.get(x.getId()))).toList();
    }

    @Override
    public List<EventShortDto> getAllPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd, Boolean onlyAvailable, EventSort sort, Pageable pageable) {
        if (rangeStart != null & rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new ValidationException("Дата начала должна быть раньше даты конца");
        }
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusDays(ExploreWithMeServer.DEFAULT_STATS_INTERVAL);
        }
        if (categories == null || categories.isEmpty() || categories.getFirst() == 0) {
            categories = categoryRepository.findAll().stream().map(Category::getId).toList();
        }
        Map<Long, Integer> confirmed = getConfirmed();
        List<Event> events = eventRepository.findByCategoryIdInAndEventDateBetweenAndStateIs(categories, rangeStart,
                        rangeEnd, EventState.PUBLISHED, pageable).stream()
                .filter(event -> text == null || text.isBlank() || event.getAnnotation().toLowerCase()
                        .contains(text.toLowerCase()) || event.getDescription().toLowerCase().contains(text.toLowerCase()))
                .filter(event -> paid == null || event.getPaid() == paid)
                .filter(event -> onlyAvailable == null || onlyAvailable.equals(false) ||
                        event.getParticipantLimit() == 0 || event.getParticipantLimit() >  confirmed.get(event.getId()))
                .toList();
        Map<Long, Long> views = getViewsStats(events);
        return events.stream().sorted((event1, event2) -> switch (sort) {
            case EVENT_DATE -> event1.getEventDate().compareTo(event2.getEventDate());
            case VIEWS ->  Math.toIntExact(views.get(event1.getId()) - views.get(event2.getId()));
            case null -> 0;
        }).map(x -> eventMapper.toEventShortDto(x, confirmed.get(x.getId()) != null ? confirmed.get(x.getId()) : 0,
                views.get(x.getId()))).toList();
    }

    @Override
    public List<EventShortDto> getFromUser(Long userId, Pageable pageable) {
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        Map<Long, Long> views = getViewsStats(events);
        Map<Long, Integer> confirmed = getConfirmed();
        return events.stream().map(x -> eventMapper.toEventShortDto(x,
                confirmed.get(x.getId()) != null ? confirmed.get(x.getId()) : 0, views.get(x.getId()))).toList();
    }

    @Override
    public EventFullDto get(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Пользователь не соответствует создателю события");
        }
        Map<Long, Integer> confirmed = getConfirmed();
        return eventMapper.toEventFullDto(event, confirmed.get(eventId) != null ? confirmed.get(eventId) : 0,
                getViewsStats(List.of(event)).get(eventId));
    }

    @Override
    public EventFullDto get(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Событие не опубликовано");
        }
        Map<Long, Integer> confirmed = getConfirmed();
        return eventMapper.toEventFullDto(event, confirmed.get(eventId) != null ? confirmed.get(eventId) : 0,
                getViewsStats(List.of(event)).get(eventId));
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
}
