package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.service.EventService;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {
    public final EventService eventService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public EventFullDto add(@PathVariable Long userId, @RequestBody @Valid NewEventDto event) {
        log.info("Добавление нового события: {}", event);
        return eventService.add(userId, event);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable Long userId, @PathVariable Long eventId,
                               @RequestBody @Valid UpdateEventUserRequest updateEventUserRequest) {
        log.info("Обновление события id={} пользователем {}", eventId, userId);
        return eventService.updateUser(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/{eventId}")
    public EventFullDto get(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Получение события id={} пользователем {}", eventId, userId);
        return eventService.get(userId, eventId);
    }

    @GetMapping
    public List<EventShortDto> getFromUser(@PathVariable Long userId, @RequestParam(defaultValue = "0") int from,
                                           @RequestParam(defaultValue = "10") int size) {
        log.info("Получение событий от пользователя id={}", userId);
        return eventService.getFromUser(userId, PageRequest.of(from, size));
    }
}
