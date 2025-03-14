package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import ru.practicum.shareit.constants.AppConstants;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerIT {

    @MockitoBean
    UserServiceImpl userService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    MockMvc mvc;

    private final List<UserDto> users = List.of(
            new UserDto(1L, "First user", "email1@mail.ru"),
            new UserDto(2L, "Second user", "email2@mail.ru")
    );

    @Test
    @DisplayName("getAllUsers должна вернуть список всех пользователей")
    void shouldGetAllUsers() throws Exception {
        when(userService.getAll())
                .thenReturn(users);

        RequestBuilder request = get("/users")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 2L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id", is(users.get(0).id()), Long.class))
                .andExpect(jsonPath("$[0].name", is(users.get(0).name())))
                .andExpect(jsonPath("$[0].email", is(users.get(0).email())))
                .andExpect(jsonPath("$[1].id", is(users.get(1).id()), Long.class))
                .andExpect(jsonPath("$[1].name", is(users.get(1).name())))
                .andExpect(jsonPath("$[1].email", is(users.get(1).email())));

        verify(userService, times(1)).getAll();
        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("get должна вернуть пользователя по id")
    void shouldGetUserById() throws Exception {
        when(userService.getById(1))
                .thenReturn(users.getFirst());

        RequestBuilder request = get("/users/1")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 2L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id", is(users.getFirst().id()), Long.class))
                .andExpect(jsonPath("$.name", is(users.getFirst().name())))
                .andExpect(jsonPath("$.email", is(users.getFirst().email())));

        verify(userService, times(1)).getById(1);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("create должна создавать пользователя")
    void shouldCreateUser() throws Exception {
        when(userService.create(any(UserDto.class)))
                .thenReturn(users.getFirst());

        RequestBuilder request = post("/users")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(users.getFirst()));

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id", is(users.getFirst().id()), Long.class))
                .andExpect(jsonPath("$.name", is(users.getFirst().name())))
                .andExpect(jsonPath("$.email", is(users.getFirst().email())));

        verify(userService, times(1)).create(users.getFirst());
        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("update должна обновлять пользователя")
    void shouldUpdateUser() throws Exception {
        when(userService.update(anyLong(), any(UserDto.class)))
                .thenReturn(users.getFirst());

        RequestBuilder request = patch("/users/2")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(users.getFirst()));

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id", is(users.getFirst().id()), Long.class))
                .andExpect(jsonPath("$.name", is(users.getFirst().name())))
                .andExpect(jsonPath("$.email", is(users.getFirst().email())));

        verify(userService, times(1)).update(2, users.getFirst());
        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("delete должна удалять пользователя")
    void shouldDeleteUser() throws Exception {
        RequestBuilder request = delete("/users/4")
                .characterEncoding(StandardCharsets.UTF_8)
                .header(AppConstants.UserIdHeader, 2L);

        mvc.perform(request)
                .andExpect(status().isNoContent());

        verify(userService, times(1)).delete(4);
        verifyNoMoreInteractions(userService);
    }
}