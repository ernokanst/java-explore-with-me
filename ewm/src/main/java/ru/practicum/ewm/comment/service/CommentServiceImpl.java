package ru.practicum.ewm.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.ExploreWithMeServer;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.storage.EventRepository;
import ru.practicum.ewm.comment.dto.*;
import ru.practicum.ewm.comment.storage.CommentRepository;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.exceptions.ValidationException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.storage.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public CommentDto add(Long userId, NewCommentDto comment) {
        User u = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Event e = eventRepository.findById(comment.getEvent()).orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (e.getState() != EventState.PUBLISHED && !Objects.equals(e.getInitiator().getId(), userId)) {
            throw new ValidationException("Событие ещё не опубликовано");
        }
        return commentMapper.toCommentDto(commentRepository.save(commentMapper.toComment(comment, e, u)));
    }

    @Override
    public CommentDto update(Long userId, CommentDto comment) {
        Comment c = commentRepository.findById(comment.getId()).orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        if (!Objects.equals(c.getAuthor().getId(), userId)) {
            throw new ValidationException("Пользователь не является автором комментария");
        }
        c.setText(comment.getText());
        return commentMapper.toCommentDto(commentRepository.save(c));
    }

    @Override
    public List<CommentDto> getForUserEvents(Long userId, LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable) {
        if (rangeStart != null & rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new ValidationException("Дата начала должна быть раньше даты конца");
        }
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now().minusDays(ExploreWithMeServer.DEFAULT_STATS_INTERVAL);
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now();
        }
        return commentRepository.findByEventInitiatorIdAndCreatedBetween(userId, rangeStart, rangeEnd, pageable)
                .stream().map(commentMapper::toCommentDto).toList();
    }

    @Override
    public List<CommentDto> getByUser(Long userId, Pageable pageable) {
        return commentRepository.findByAuthorId(userId, pageable).stream().map(commentMapper::toCommentDto).toList();
    }

    @Override
    public List<CommentDto> get(LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable) {
        if (rangeStart != null & rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new ValidationException("Дата начала должна быть раньше даты конца");
        }
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now().minusDays(ExploreWithMeServer.DEFAULT_STATS_INTERVAL);
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now();
        }
        return commentRepository.findByCreatedBetween(rangeStart, rangeEnd, pageable)
                .stream().map(commentMapper::toCommentDto).toList();
    }

    @Override
    public void adminDelete(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Override
    public void userDelete(Long userId, Long commentId) {
        Comment c = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        if (!Objects.equals(c.getAuthor().getId(), userId)) {
            throw new ValidationException("Пользователь не является автором комментария");
        }
        commentRepository.deleteById(commentId);
    }
}
