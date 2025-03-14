package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Test
    void getAllShouldReturnDataWhenInvoked() {
        User expectedUser = new User(1L, "Name", "email");
        UserDto expectedUserDto = new UserDto(1L, "Name", "email");

        when(userRepository.findAll()).thenReturn(List.of(expectedUser));
        when(userMapper.toUserDto(expectedUser)).thenReturn(expectedUserDto);

        List<UserDto> result = userService.getAll();

        assertEquals(result.getFirst(), expectedUserDto, "Возвращённые данные не соответсвуют ожидаемым");
        verify(userMapper, times(1)).toUserDto(expectedUser);
        verify(userRepository, times(1)).findAll();
        verifyNoMoreInteractions(userMapper, userService);
    }

    @Test
    void getByIdShouldReturnDataWhenUserFound() {
        User expectedUser = new User(1L, "Name", "email");
        UserDto expectedUserDto = new UserDto(1L, "Name", "email");

        when(userRepository.findById(1)).thenReturn(Optional.of(expectedUser));
        when(userMapper.toUserDto(expectedUser)).thenReturn(expectedUserDto);

        UserDto result = userService.getById(1);

        assertEquals(result, expectedUserDto, "Возвращённые данные не соответсвуют ожидаемым");
        verify(userMapper, times(1)).toUserDto(expectedUser);
        verify(userRepository, times(1)).findById(1);
        verifyNoMoreInteractions(userMapper, userRepository);
    }

    @Test
    void getByIdShouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getById(1));

        assertEquals(exception.getMessage(), "Поользователь 1 не найден");
        verify(userRepository, times(1)).findById(1);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userMapper);
    }

    @Test
    void createShouldCreateUserWhenInputDataCorrect() {
        User expectedUser = new User(1L, "Name", "email");
        UserDto expectedUserDto = new UserDto(1L, "Name", "email");

        when(userRepository.save(expectedUser)).thenReturn(expectedUser);
        when(userMapper.toUserDto(expectedUser)).thenReturn(expectedUserDto);
        when(userMapper.toUser(expectedUserDto)).thenReturn(expectedUser);

        UserDto result = userService.create(expectedUserDto);

        assertEquals(result, expectedUserDto, "Возвращённые данные не соответсвуют ожидаемым");
        verify(userMapper, times(1)).toUserDto(expectedUser);
        verify(userMapper, times(1)).toUser(expectedUserDto);
        verify(userRepository, times(1)).findByEmail("email");
        verify(userRepository, times(1)).save(expectedUser);
        verifyNoMoreInteractions(userMapper, userRepository);
    }

    @Test
    void createShouldNotCreateUserWhenInputDataIncorrect() {
        UserDto userDto = new UserDto(1L, "Name", "email");
        User user = new User(1L, "Name", "email");

        when(userRepository.findByEmail("email")).thenReturn(Optional.of(user));

        AlreadyExistException exception = assertThrows(AlreadyExistException.class, () -> userService.create(userDto));
        assertEquals(exception.getMessage(), "Пользователь с почтой email уже существует");
        verify(userRepository, times(1)).findByEmail("email");
        verifyNoMoreInteractions(userMapper, userRepository);
    }

    @Test
    void updateShouldUpdateUserDataWhenUserFound() {
        User oldUser = new User(1L, "oldName", "oldMail");
        User newUser = new User(1L, "newName", "newMail");
        UserDto newUserDto = new UserDto(1L, "newName", "newMail");
        when(userRepository.findById(1)).thenReturn(Optional.of(oldUser));

        userService.update(1, newUserDto);

        verify(userRepository).save(userArgumentCaptor.capture());
        User updatedUser = userArgumentCaptor.getValue();
        assertEquals(updatedUser.getName(), newUser.getName(), "Имя пользователя не обновлено");
        assertEquals(updatedUser.getEmail(), newUser.getEmail(), "Почта пользователя не обновлена");
    }

    @Test
    void delete() {
    }

    @Test
    void getUserById() {
    }
}