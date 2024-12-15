package ru.practicum.ewm.comment.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class NewCommentDto {
    @NotBlank
    @Size(min = 2, max = 2000)
    private String text;
    @NotNull
    private Long event;
}
