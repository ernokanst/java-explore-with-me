package ru.practicum.ewm.event.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.EventSort;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto add(Long userId, NewEventDto event);

    EventFullDto updateAdmin(Long eventId, UpdateEventAdminRequest event);

    EventFullDto updateUser(Long userId, Long eventId, UpdateEventUserRequest event);

    List<EventFullDto> getAllAdmin(List<Long> users, List<String> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);

    List<EventShortDto> getAllPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, EventSort sort, Pageable pageable);

    List<EventShortDto> getFromUser(Long userId, Pageable pageable);

    EventFullDto get(Long userId, Long eventId);

    EventFullDto get(Long eventId);
}
