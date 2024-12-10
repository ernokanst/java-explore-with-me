package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.storage.CategoryRepository;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.*;
import ru.practicum.ewm.event.storage.EventRepository;
import ru.practicum.ewm.exceptions.ConflictException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.exceptions.ValidationException;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.storage.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.storage.UserRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.ViewStatsDto;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final StatsClient stats;

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
        return eventMapper.toEventFullDto(eventRepository.save(eventMapper.toAdminUpdatedEvent(e, event)),
                confirmedRequests(eventId),
                getViewsStats(List.of(e)).get(eventId));
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
        return eventMapper.toEventFullDto(eventRepository.save(eventMapper.toUserUpdatedEvent(e, event)),
                confirmedRequests(eventId),
                getViewsStats(List.of(e)).get(eventId));
    }

    @Override
    public List<EventFullDto> getAllAdmin(List<Long> users, List<String> states, List<Long> categories,
                                          LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable) {
        if (users == null || users.isEmpty()) {
            users = userRepository.findAll().stream().map(User::getId).toList();
        }
        if (categories == null || categories.isEmpty()) {
            categories = categoryRepository.findAll().stream().map(Category::getId).toList();
        }
        List<Long> u = users;
        List<Long> c = categories;
        List<Event> events = eventRepository.findAll().stream()
                .filter(event -> u.contains(event.getInitiator().getId()))
                .filter(event -> states == null || states.isEmpty() || states.contains(event.getState().toString()))
                .filter(event -> c.contains(event.getCategory().getId()))
                .filter(event -> rangeStart == null || event.getEventDate().isAfter(rangeStart))
                .filter(event -> rangeEnd == null || event.getEventDate().isBefore(rangeEnd))
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .toList();
        Map<Long, Long> views = getViewsStats(events);
        return events.stream().map(x -> eventMapper.toEventFullDto(x, confirmedRequests(x.getId()), views.get(x.getId()))).toList();
    }

    @Override
    public List<EventShortDto> getAllPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd, Boolean onlyAvailable, EventSort sort, Pageable pageable) {
        if (rangeStart != null & rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new ValidationException("Дата начала должна быть раньше даты конца");
        }
        List<Event> events = eventRepository.findAll().stream()
                .filter(event -> event.getState().equals(EventState.PUBLISHED))
                .filter(event -> text == null || text.isBlank() || event.getAnnotation().toLowerCase().contains(text.toLowerCase()) || event.getDescription().toLowerCase().contains(text.toLowerCase()))
                .filter(event -> categories == null || categories.isEmpty() || categories.contains(event.getCategory().getId()))
                .filter(event -> paid == null || event.getPaid() == paid)
                .filter(event -> ((rangeStart == null && event.getEventDate().isAfter(LocalDateTime.now())) || event.getEventDate().isAfter(rangeStart)))
                .filter(event -> rangeEnd == null || event.getEventDate().isBefore(rangeEnd))
                .filter(event -> onlyAvailable == null || onlyAvailable.equals(false) || event.getParticipantLimit() == 0 || event.getParticipantLimit() >  confirmedRequests(event.getId()))
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .toList();
        Map<Long, Long> views = getViewsStats(events);
        return events.stream().sorted((event1, event2) -> {
            return switch (sort) {
                case EVENT_DATE -> event1.getEventDate().compareTo(event2.getEventDate());
                case VIEWS ->  Math.toIntExact(views.get(event1.getId()) - views.get(event2.getId()));
                case null -> 0;
            };
        }).map(x -> eventMapper.toEventShortDto(x, confirmedRequests(x.getId()), views.get(x.getId()))).toList();
    }

    @Override
    public List<EventShortDto> getFromUser(Long userId, Pageable pageable) {
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        Map<Long, Long> views = getViewsStats(events);
        return events.stream().map(x -> eventMapper.toEventShortDto(x, confirmedRequests(x.getId()), views.get(x.getId()))).toList();
    }

    @Override
    public EventFullDto get(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Пользователь не соответствует создателю события");
        }
        return eventMapper.toEventFullDto(event, confirmedRequests(eventId), getViewsStats(List.of(event)).get(eventId));
    }

    @Override
    public EventFullDto get(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Событие не опубликовано");
        }
        return eventMapper.toEventFullDto(event, confirmedRequests(eventId), getViewsStats(List.of(event)).get(eventId));
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
