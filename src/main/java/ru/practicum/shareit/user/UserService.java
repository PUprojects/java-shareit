package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAll();

    UserDto getById(long userId);

    UserDto create(UserDto user);

    UserDto update(long userId, UserDto user);

    void delete(long userId);

    User getUserById(long userId);
}
