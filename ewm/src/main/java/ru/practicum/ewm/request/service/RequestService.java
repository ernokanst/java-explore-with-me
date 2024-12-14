package ru.practicum.ewm.request.service;

import ru.practicum.ewm.request.dto.*;
import java.util.List;

public interface RequestService {
    ParticipationRequestDto create(Long userId, Long eventId);

    ParticipationRequestDto cancel(Long userId, Long requestId);

    List<ParticipationRequestDto> getAll(Long userId);

    List<ParticipationRequestDto> getForEvent(Long eventId);

    EventRequestStatusUpdateResult update(Long userId, Long eventId, EventRequestStatusUpdateRequest request);
}
