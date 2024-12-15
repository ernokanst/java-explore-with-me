package ru.practicum.ewm.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.ExploreWithMeServer;
import ru.practicum.ewm.comment.dto.*;
import ru.practicum.ewm.comment.service.CommentService;
import java.time.LocalDateTime;
import java.util.List;

@Validated
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users/{userId}/comments")
public class PrivateCommentController {
    private final CommentService commentService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CommentDto add(@PathVariable Long userId, @RequestBody @Valid NewCommentDto comment) {
        log.info("Добавление комментария пользователем id={}: {}", userId, comment);
        return commentService.add(userId, comment);
    }

    @PatchMapping
    public CommentDto update(@PathVariable Long userId, @RequestBody @Valid CommentDto comment) {
        log.info("Редактирование комментария id={} пользователем id={}", comment.getId(), userId);
        return commentService.update(userId, comment);
    }

    @GetMapping("/events")
    public List<CommentDto> getForUserEvents(@PathVariable Long userId,
                                             @RequestParam(required = false) @DateTimeFormat(pattern = ExploreWithMeServer.DATE_FORMAT)
                                             LocalDateTime rangeStart,
                                             @RequestParam(required = false) @DateTimeFormat(pattern = ExploreWithMeServer.DATE_FORMAT)
                                             LocalDateTime rangeEnd,
                                             @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                             @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Получение комментариев к событиям пользователя id={} от {} до {}", userId, rangeStart, rangeEnd);
        return commentService.getForUserEvents(userId, rangeStart, rangeEnd, PageRequest.of(from, size,
                Sort.by(Sort.Direction.ASC, "id")));
    }

    @GetMapping
    public List<CommentDto> getByUser(@PathVariable Long userId,
                                      @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                      @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Получение комментариев от пользователя id={}", userId);
        return commentService.getByUser(userId, PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "id")));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{commentId}")
    public void delete(@PathVariable Long userId, @PathVariable Long commentId) {
        log.info("Удаление комментария id={} пользователем id={}", commentId, userId);
        commentService.userDelete(userId, commentId);
    }
}
