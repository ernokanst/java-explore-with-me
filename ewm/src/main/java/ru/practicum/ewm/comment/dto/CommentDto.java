package ru.practicum.ewm.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.ewm.ExploreWithMeServer;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    @NotNull
    private Long id;
    @JsonFormat(pattern = ExploreWithMeServer.DATE_FORMAT)
    private LocalDateTime created;
    @NotBlank
    @Size(min = 2, max = 2000)
    private String text;
    private Long event;
    private Long author;
}
