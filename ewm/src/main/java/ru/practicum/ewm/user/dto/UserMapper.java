package ru.practicum.ewm.user.dto;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.user.model.User;

@Component
public class UserMapper {
    public UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public UserShortDto toUserShortDto(User user) {
        return new UserShortDto(
                user.getId(),
                user.getName()
        );
    }

    public User toUser(NewUserRequest user) {
        return new User(
                null,
                user.getName(),
                user.getEmail()
        );
    }
}
