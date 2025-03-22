package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {
    private final UserMapper userMapper = new UserMapperImpl();

    @Test
    void toUserDtoShouldReturnNullWhenInParameterIsNull() {
        assertNull(userMapper.toUserDto(null), "Возвращаемое значение должно быть null");
    }

    @Test
    void toUserShouldReturnNullWhenInParameterIsNull() {
        assertNull(userMapper.toUser(null), "Возвращаемое значение должно быть null");
    }

    @Test
    void toUserDtoShouldMapToDto() {
        User user = new User(1, "First user", "first@mail.ru");

        UserDto userDto = userMapper.toUserDto(user);

        assertEquals(user.getId(), userDto.id(), "id не совпадает");
        assertEquals(user.getName(), userDto.name(), "Имя не совпадает");
        assertEquals(user.getEmail(), userDto.email(), "Почта не совпадает");
    }

    @Test
    void toUserShouldMapToUser() {
        UserDto userDto = new UserDto(1L, "First user", "first@mail.ru");

        User user = userMapper.toUser(userDto);
        assertEquals(userDto.id(), user.getId(), "id не совпадает");
        assertEquals(userDto.name(), user.getName(), "Имя не совпадает");
        assertEquals(userDto.email(), user.getEmail(), "Почта не совпадает");
    }
}