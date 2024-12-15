package ru.practicum.ewm.comment.dto;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.user.model.User;
import java.time.LocalDateTime;

@Component
public class CommentMapper {
    public CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getCreated(),
                comment.getText(),
                comment.getEvent().getId(),
                comment.getAuthor().getId());
    }

    public Comment toComment(NewCommentDto comment, Event event, User author) {
        return new Comment(null, LocalDateTime.now(), comment.getText(), event, author);
    }
}
