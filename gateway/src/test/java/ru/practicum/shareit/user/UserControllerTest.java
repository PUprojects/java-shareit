package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.constants.AppConstants;
import ru.practicum.shareit.user.dto.UserDto;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    static class BadUsersArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of(new UserDto(1L, "", "first@email.ru")),
                    Arguments.of(new UserDto(1L, "First Name", null)),
                    Arguments.of(new UserDto(1L, "First Name", "first.ru"))
            );
        }
    }

    @Mock
    UserClient userClient;

    @InjectMocks
    UserController userController;

    private final ObjectMapper mapper = new ObjectMapper();

    MockMvc mvc;

    private final List<UserDto> users = List.of(
            new UserDto(1L, "First User", "first@mail.ru"),
            new UserDto(2L, "Second user", "second@mail.ru"));

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(userController)
                .build();
    }

    @Test
    @DisplayName("getAllUsers должна вернуть список всех пользователей")
    void shouldGetAllUsers() throws Exception {
        when(userClient.getAll())
                .thenReturn(ResponseEntity.ok().body(users));

        RequestBuilder request = get("/users")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.USER_ID_HEADER, 2L);

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

        verify(userClient, times(1)).getAll();
        verifyNoMoreInteractions(userClient);
    }

    @Test
    @DisplayName("get должна вернуть пользователя по id")
    void shouldGetUserById() throws Exception {
        when(userClient.getById(1))
                .thenReturn(ResponseEntity.ok().body(users.getFirst()));

        RequestBuilder request = get("/users/1")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.USER_ID_HEADER, 2L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id", is(users.getFirst().id()), Long.class))
                .andExpect(jsonPath("$.name", is(users.getFirst().name())))
                .andExpect(jsonPath("$.email", is(users.getFirst().email())));

        verify(userClient, times(1)).getById(1);
        verifyNoMoreInteractions(userClient);
    }

    @Test
    @DisplayName("create должна создавать пользователя, когда входные данные корректны")
    void shouldCreateUserWhenInputDataCorrect() throws Exception {
        when(userClient.create(any(UserDto.class)))
                .thenReturn(ResponseEntity.created(URI.create("users/1")).body(users.getFirst()));

        RequestBuilder request = post("/users")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.USER_ID_HEADER, 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(users.getFirst()));

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id", is(users.getFirst().id()), Long.class))
                .andExpect(jsonPath("$.name", is(users.getFirst().name())))
                .andExpect(jsonPath("$.email", is(users.getFirst().email())));

        verify(userClient, times(1)).create(users.getFirst());
        verifyNoMoreInteractions(userClient);
    }

    @ParameterizedTest(name = "{index} - create не должна создавать пользователя, когда входные данные не корректны")
    @ArgumentsSource(BadUsersArgumentsProvider.class)
    void shouldNotCreateUserWhenInputDataIncorrect(UserDto inputBadUser) throws Exception {

        RequestBuilder request = post("/users")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.USER_ID_HEADER, 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(inputBadUser));

        mvc.perform(request)
                        .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }
}