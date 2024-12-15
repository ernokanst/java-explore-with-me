package ru.practicum.ewm.comment.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.comment.model.Comment;
import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByEventInitiatorIdAndCreatedBetween(Long userId, LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                        Pageable pageable);

    List<Comment> findByAuthorId(Long userId, Pageable pageable);

    List<Comment> findByCreatedBetween(LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);

    List<Comment> findByEventId(Long id);

    List<Comment> findByEventIdIn(List<Long> ids);
}
