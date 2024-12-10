package ru.practicum.ewm.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.dto.*;
import ru.practicum.ewm.request.service.RequestService;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users/{userId}")
public class RequestController {
    private final RequestService requestService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/requests")
    public ParticipationRequestDto create(@PathVariable Long userId, @RequestParam Long eventId) {
        log.info("Создан запрос на участие в событии id={} пользователем id={}", eventId, userId);
        return requestService.create(userId, eventId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ParticipationRequestDto cancel(@PathVariable Long userId, @PathVariable Long requestId) {
        log.info("Запрос id={} на участие в событии отклонён", requestId);
        return requestService.cancel(userId, requestId);
    }

    @GetMapping("/requests")
    public List<ParticipationRequestDto> getAll(@PathVariable Long userId) {
        log.info("Получение запросов на участие пользователя id={}", userId);
        return requestService.getAll(userId);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<ParticipationRequestDto> getForEvent(@PathVariable Long eventId) {
        log.info("Получение запросов на участие в событии id={}", eventId);
        return requestService.getForEvent(eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult update(@PathVariable Long userId, @PathVariable Long eventId,
                                                 @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("Обновление запросов на участие в событии id={}", eventId);
        return requestService.update(userId, eventId, request);
    }
}
