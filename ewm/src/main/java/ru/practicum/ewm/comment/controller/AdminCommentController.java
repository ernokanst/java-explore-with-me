package ru.practicum.ewm.comment.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.ExploreWithMeServer;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.service.CommentService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/comments")
public class AdminCommentController {
    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> get(@RequestParam(required = false) @DateTimeFormat(pattern = ExploreWithMeServer.DATE_FORMAT)
                                LocalDateTime rangeStart,
                                @RequestParam(required = false) @DateTimeFormat(pattern = ExploreWithMeServer.DATE_FORMAT)
                                LocalDateTime rangeEnd,
                                @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Получение комментариев к событиям администратором от {} до {}", rangeStart, rangeEnd);
        return commentService.get(rangeStart, rangeEnd, PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "id")));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Удаление комментария id={} администратором", id);
        commentService.adminDelete(id);
    }
}
