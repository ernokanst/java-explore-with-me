package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.storage.EventRepository;
import ru.practicum.ewm.exceptions.*;
import ru.practicum.ewm.request.dto.*;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.storage.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.storage.UserRepository;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public ParticipationRequestDto create(Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (event.getInitiator().equals(user)) {
            throw new ConflictException("Запрос на участие в собственном событии");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Запрос на участие в неопубликованном событии");
        }
        if (event.getParticipantLimit() > 0 && requestRepository.countAllByEventIdAndStatus(eventId,
                RequestStatus.CONFIRMED) >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников");
        }
        Request request = requestMapper.toRequest(event, user);
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        }
        return requestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Запрос не найден"));
        if (!Objects.equals(request.getRequester().getId(), userId)) {
            throw new ValidationException("Пользователь не совпадает с создателем запроса");
        }
        request.setStatus(RequestStatus.CANCELED);
        return requestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getAll(Long userId) {
        return requestRepository.findAllByRequesterId(userId).stream().map(requestMapper::toParticipationRequestDto).toList();
    }

    @Override
    public List<ParticipationRequestDto> getForEvent(Long eventId) {
        return requestRepository.findAllByEventId(eventId).stream().map(requestMapper::toParticipationRequestDto).toList();
    }

    @Override
    public EventRequestStatusUpdateResult update(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие не найдено"));
        List<Request> requests = requestRepository.findAllById(request.getRequestIds());
        List<Request> requestsForConfirmation = new ArrayList<>();
        List<Request> requestsForRejection = new ArrayList<>();
        if (request.getStatus() == RequestStatus.CONFIRMED) {
            if (event.getParticipantLimit() > 0) {
                int available = event.getParticipantLimit() - requestRepository
                        .countAllByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
                if (available <= 0) {
                    throw new ConflictException("Достигнут лимит участников");
                }
                if (request.getRequestIds().size() < available) {
                    requestsForConfirmation.addAll(requests);
                } else {
                    requestsForConfirmation.addAll(requests.subList(0, available));
                    requestsForRejection.addAll(requests.subList(available, requests.size()));
                }
            } else {
                requestsForConfirmation.addAll(requests);
            }
        } else {
            for (Request r : requests) {
                if (r.getStatus().equals(RequestStatus.CONFIRMED)) {
                    throw new ConflictException("Заявка уже принята");
                }
            }
            requestsForRejection.addAll(requests);
        }
        requestsForConfirmation.forEach(r -> r.setStatus(RequestStatus.CONFIRMED));
        requestRepository.saveAll(requestsForConfirmation);
        List<ParticipationRequestDto> confirmedRequests = requestsForConfirmation.stream()
                .map(requestMapper::toParticipationRequestDto).toList();
        if (Objects.equals(requestRepository.countAllByEventIdAndStatus(eventId, RequestStatus.CONFIRMED),
                event.getParticipantLimit())) {
            List<Request> allPendingRequests = requestRepository.findAllByEventIdAndStatus(eventId, RequestStatus.PENDING);
            requestsForRejection.addAll(allPendingRequests);
        }
        requestsForRejection.forEach(r -> r.setStatus(RequestStatus.REJECTED));
        requestRepository.saveAll(requestsForRejection);
        List<ParticipationRequestDto> rejectedRequests = requestsForRejection.stream()
                .map(requestMapper::toParticipationRequestDto).toList();
        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }
}
