package ru.practicum.ewm.comment.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.comment.dto.*;
import java.time.LocalDateTime;
import java.util.List;

public interface CommentService {
    CommentDto add(Long userId, NewCommentDto comment);

    CommentDto update(Long userId, CommentDto comment);

    List<CommentDto> getForUserEvents(Long userId, LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);

    List<CommentDto> getByUser(Long userId, Pageable pageable);

    List<CommentDto> get(LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);

    void adminDelete(Long commentId);

    void userDelete(Long userId, Long commentId);
}
