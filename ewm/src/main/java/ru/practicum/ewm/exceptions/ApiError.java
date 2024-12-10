package ru.practicum.ewm.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.ewm.ExploreWithMeServer;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {
    private String errors;
    private String message;
    private String reason;
    private String status;
    @JsonFormat(pattern = ExploreWithMeServer.DATE_FORMAT)
    private LocalDateTime timestamp;
}
