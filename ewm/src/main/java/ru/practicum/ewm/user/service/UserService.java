package ru.practicum.ewm.user.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.user.dto.*;
import java.util.List;

public interface UserService {
    UserDto add(NewUserRequest user);

    List<UserDto> getAll(List<Long> ids, Pageable pageable);

    void delete(Long id);
}
