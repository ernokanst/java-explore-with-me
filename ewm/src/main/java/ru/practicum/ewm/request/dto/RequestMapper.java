package ru.practicum.ewm.request.dto;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.user.model.User;

@Component
public class RequestMapper {
    public ParticipationRequestDto toParticipationRequestDto(Request request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getCreated(),
                request.getEvent().getId(),
                request.getRequester().getId(),
                request.getStatus());
    }

    public Request toRequest(Event event, User requester) {
        return new Request(null, null, event, requester,
                event.getParticipantLimit() > 0 ? RequestStatus.PENDING : RequestStatus.CONFIRMED);
    }
}
