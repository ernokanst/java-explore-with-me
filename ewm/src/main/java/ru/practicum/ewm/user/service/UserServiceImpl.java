package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.user.dto.*;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.user.storage.UserRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto add(NewUserRequest user) {
        return userMapper.toUserDto(userRepository.save(userMapper.toUser(user)));
    }

    @Override
    public List<UserDto> getAll(List<Long> ids, Pageable pageable) {
        if (ids != null) {
            return userRepository.findAllById(ids).stream().map(userMapper::toUserDto).toList();
        } else {
            return userRepository.findAll(pageable).getContent().stream().map(userMapper::toUserDto).toList();
        }
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
