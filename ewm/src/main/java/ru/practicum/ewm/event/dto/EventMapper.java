package ru.practicum.ewm.event.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.category.dto.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.model.*;
import ru.practicum.ewm.user.dto.UserMapper;
import ru.practicum.ewm.user.model.User;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EventMapper {
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    public Event toEvent(NewEventDto event, User initiator, Category category) {
        return new Event(
                null,
                event.getAnnotation(),
                category,
                LocalDateTime.now(),
                event.getDescription(),
                event.getEventDate(),
                initiator,
                event.getLocation(),
                event.getPaid(),
                event.getParticipantLimit(),
                null,
                event.getRequestModeration(),
                EventState.PENDING,
                event.getTitle()
        );
    }

    public EventFullDto toEventFullDto(Event event, Integer confirmedRequests, Long views) {
        return new EventFullDto(
                event.getId(),
                event.getAnnotation(),
                categoryMapper.toCategoryDto(event.getCategory()),
                confirmedRequests,
                event.getCreatedOn(),
                event.getDescription(),
                event.getEventDate(),
                userMapper.toUserShortDto(event.getInitiator()),
                event.getLocation(),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.getRequestModeration(),
                event.getState(),
                event.getTitle(),
                views
        );
    }

    public EventShortDto toEventShortDto(Event event, Integer confirmedRequests, Long views) {
        return new EventShortDto(
                event.getId(),
                event.getAnnotation(),
                categoryMapper.toCategoryDto(event.getCategory()),
                confirmedRequests,
                event.getEventDate(),
                userMapper.toUserShortDto(event.getInitiator()),
                event.getPaid(),
                event.getTitle(),
                views
        );
    }

    public Event toAdminUpdatedEvent(Event event, UpdateEventAdminRequest adminUpdate) {
        if (adminUpdate.getAnnotation() != null) {
            event.setAnnotation(adminUpdate.getAnnotation());
        }
        if (adminUpdate.getCategory() != null) {
            event.setCategory(new Category(adminUpdate.getCategory(), null));
        }
        if (adminUpdate.getDescription() != null) {
            event.setDescription(adminUpdate.getDescription());
        }
        if (adminUpdate.getEventDate() != null) {
            event.setEventDate(adminUpdate.getEventDate());
        }
        if (adminUpdate.getLocation() != null) {
            event.setLocation(adminUpdate.getLocation());
        }
        if (adminUpdate.getPaid() != null) {
            event.setPaid(adminUpdate.getPaid());
        }
        if (adminUpdate.getParticipantLimit() != null) {
            event.setParticipantLimit(adminUpdate.getParticipantLimit());
        }
        if (adminUpdate.getRequestModeration() != null) {
            event.setRequestModeration(adminUpdate.getRequestModeration());
        }
        if (adminUpdate.getStateAction() != null) {
            if (adminUpdate.getStateAction().equals(AdminStateAction.PUBLISH_EVENT)) {
                event.setState(EventState.PUBLISHED);
            }
            if (adminUpdate.getStateAction().equals(AdminStateAction.REJECT_EVENT)) {
                event.setState(EventState.CANCELED);
            }
        }
        if (adminUpdate.getTitle() != null) {
            event.setTitle(adminUpdate.getTitle());
        }
        return event;
    }

    public Event toUserUpdatedEvent(Event event, UpdateEventUserRequest userUpdate) {
        if (userUpdate.getAnnotation() != null) {
            event.setAnnotation(userUpdate.getAnnotation());
        }
        if (userUpdate.getCategory() != null) {
            event.setCategory(new Category(userUpdate.getCategory(), null));
        }
        if (userUpdate.getDescription() != null) {
            event.setDescription(userUpdate.getDescription());
        }
        if (userUpdate.getEventDate() != null) {
            event.setEventDate(userUpdate.getEventDate());
        }
        if (userUpdate.getLocation() != null) {
            event.setLocation(userUpdate.getLocation());
        }
        if (userUpdate.getPaid() != null) {
            event.setPaid(userUpdate.getPaid());
        }
        if (userUpdate.getParticipantLimit() != null) {
            event.setParticipantLimit(userUpdate.getParticipantLimit());
        }
        if (userUpdate.getRequestModeration() != null) {
            event.setRequestModeration(userUpdate.getRequestModeration());
        }
        if (userUpdate.getStateAction() != null) {
            if (userUpdate.getStateAction().equals(UserStateAction.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            }
            if (userUpdate.getStateAction().equals(UserStateAction.CANCEL_REVIEW)) {
                event.setState(EventState.CANCELED);
            }
        }
        if (userUpdate.getTitle() != null) {
            event.setTitle(userUpdate.getTitle());
        }
        return event;
    }
}
