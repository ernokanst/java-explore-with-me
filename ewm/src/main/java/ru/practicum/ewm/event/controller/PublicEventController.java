package ru.practicum.ewm.event.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.ExploreWithMeServer;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.EventSort;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.stats.client.StatsClient;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.stats.dto.EndpointHitDto;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/events")
public class PublicEventController {
    private final EventService eventsService;
    private final StatsClient stats;

    @GetMapping
    public List<EventShortDto> getAll(@RequestParam(required = false) String text,
                                      @RequestParam(required = false) List<Long> categories,
                                      @RequestParam(required = false) Boolean paid,
                                      @RequestParam(required = false) @DateTimeFormat(pattern = ExploreWithMeServer.DATE_FORMAT)
                                      LocalDateTime rangeStart,
                                      @RequestParam(required = false) @DateTimeFormat(pattern = ExploreWithMeServer.DATE_FORMAT)
                                      LocalDateTime rangeEnd,
                                      @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                      @RequestParam(defaultValue = "EVENT_DATE") EventSort sort,
                                      @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                      @RequestParam(defaultValue = "10") @Positive int size,
                                      HttpServletRequest request) {
        stats.addHit(new EndpointHitDto(null, "ewm-main-service", "/events",
                request.getRemoteAddr(), LocalDateTime.now()));
        log.info("Получение событий пользователем. Текст: {}, категории: {}, оплачено: {}, от: {}, до: {}, только доступ" +
                "ные: {}, сортировка: {}", text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort);
        return eventsService.getAllPublic(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort,
                PageRequest.of(from, size));
    }

    @GetMapping("/{id}")
    public EventFullDto get(@PathVariable Long id, HttpServletRequest request) {
        stats.addHit(new EndpointHitDto(null, "ewm-main-service", "/events/" + id,
                request.getRemoteAddr(), LocalDateTime.now()));
        log.info("Получение события id={}", id);
        return eventsService.get(id);
    }
}
