package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.ExploreWithMeServer;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.service.EventService;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/events")
public class AdminEventController {
    private final EventService eventService;

    @PatchMapping("/{id}")
    public EventFullDto update(@PathVariable Long id, @RequestBody @Valid UpdateEventAdminRequest request) {
        log.info("Обновление события id={} администратором", id);
        return eventService.updateAdmin(id, request);
    }

    @GetMapping
    List<EventFullDto> getAll(@RequestParam(required = false) List<Long> users,
                              @RequestParam(required = false) List<String> states,
                              @RequestParam(required = false) List<Long> categories,
                              @RequestParam(required = false) @DateTimeFormat(pattern = ExploreWithMeServer.DATE_FORMAT)
                              LocalDateTime rangeStart,
                              @RequestParam(required = false) @DateTimeFormat(pattern = ExploreWithMeServer.DATE_FORMAT)
                              LocalDateTime rangeEnd,
                              @RequestParam(defaultValue = "0") int from, @RequestParam(defaultValue = "10") int size) {
        log.info("Получение событий администратором. Пользователи: {}, состояния: {}, категории: {}, от: {}, до: {}",
                users, states, categories, rangeStart, rangeEnd);
        return eventService.getAllAdmin(users, states, categories, rangeStart, rangeEnd, PageRequest.of(from, size));
    }
}
