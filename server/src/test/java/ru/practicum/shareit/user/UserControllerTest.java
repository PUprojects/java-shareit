package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void getAllUsersShouldReturnDataWhenInvoked() {
        List<UserDto> expectedData = List.of(new UserDto(1L, "Name", "mail"));
        when(userService.getAll()).thenReturn(expectedData);

        List<UserDto> result =  userController.getAllUsers();

        assertEquals(expectedData, result, "Возвращённые данные не соответсвуют ожидаемым");
        verify(userService, Mockito.times(1)).getAll();
        verifyNoMoreInteractions(userService);
    }

    @Test
    void getShouldReturnDataWhenInvoked() {
        UserDto expectedData = new UserDto(1L, "name", "mail");
        when(userService.getById(1)).thenReturn(expectedData);

        UserDto result = userController.get(1);

        assertEquals(expectedData, result, "Возвращённые данные не соответсвуют ожидаемым");
        verify(userService, Mockito.times(1)).getById(1);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void createShouldReturnCreatedUserWhenInvoked() {
        UserDto expectedUser = new UserDto(1L, "name", "mail");
        when(userService.create(expectedUser)).thenReturn(expectedUser);

        UserDto result = userController.create(expectedUser);

        assertEquals(result, expectedUser, "Возвращённые данные не соответсвуют ожидаемым");
        verify(userService, times(1)).create(expectedUser);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void updateShouldReturnDataWhenInvoked() {
        UserDto expectedUser = new UserDto(1L, "name", "mail");
        when(userService.update(1, expectedUser)).thenReturn(expectedUser);

        UserDto result = userController.update(1, expectedUser);

        assertEquals(result, expectedUser, "Возвращённые данные не соответсвуют ожидаемым");
        verify(userService, times(1)).update(1, expectedUser);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void deleteShouldCallServiceDeleteWhenInvoked() {
        userService.delete(1);

        verify(userService,times(1)).delete(1);
        verifyNoMoreInteractions(userService);
    }
}