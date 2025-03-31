package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.Strings;
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
import ru.practicum.shareit.request.dto.ItemRequestDto;

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
class RequestControllerTest {

    static class BadRequestsArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of(new ItemRequestDto("")),
                    Arguments.of(new ItemRequestDto(Strings.repeat("t", 256)))
            );
        }
    }

    @Mock
    RequestClient requestClient;

    @InjectMocks
    RequestController requestController;

    private final ObjectMapper mapper = new ObjectMapper();

    private final List<ItemRequestDto> requests = List.of(
            new ItemRequestDto("First Request"),
            new ItemRequestDto("Second Request"));
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(requestController)
                .build();
    }

    @Test
    @DisplayName("addNewRequest должна создавать запрос вещи когда входные данные корректны")
    void shouldCreateItemRequestWhenInputDataCorrect() throws Exception {
        when(requestClient.add(2, requests.getFirst()))
                .thenReturn(ResponseEntity.created(URI.create("/requests/1")).body(requests.getFirst()));

        RequestBuilder request = post("/requests")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.USER_ID_HEADER, 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requests.getFirst()));

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.description", is(requests.getFirst().description())));

        verify(requestClient, times(1)).add(2, requests.getFirst());
        verifyNoMoreInteractions(requestClient);
    }

    @Test
    @DisplayName("addNewRequest не должна создавать запрос вещи когда не указан id пользователя")
    void shouldNotCreateNewItemRequestWhenUserIdNotSent() throws Exception {
        RequestBuilder request = post("/requests")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requests.getFirst()));

        mvc.perform(request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestClient);
    }

    @ParameterizedTest(name = "{index} - create не должна создавать пользователя, когда входные данные не корректны")
    @ArgumentsSource(RequestControllerTest.BadRequestsArgumentsProvider.class)
    void shouldNotCreateUserWhenInputDataIncorrect(ItemRequestDto inputBadRequest) throws Exception {

        RequestBuilder request = post("/requests")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.USER_ID_HEADER, 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(inputBadRequest));

        mvc.perform(request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestClient);
    }

    @Test
    @DisplayName("getUserRequests должна вернуть все запросы указанного пользователя")
    void shouldGetAllRequestsByUserId() throws Exception {
        when(requestClient.getAllForUser(2))
                .thenReturn(ResponseEntity.ok().body(requests));

        RequestBuilder request = get("/requests")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.USER_ID_HEADER, 2L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].description", is(requests.get(0).description())))
                .andExpect(jsonPath("$[1].description", is(requests.get(1).description())));

        verify(requestClient, times(1)).getAllForUser(2);
        verifyNoMoreInteractions(requestClient);
    }

    @Test
    @DisplayName("getUserRequests не должна возвращать данные когда не указан id пользователя")
    void shouldNotGetAllRequestsByUserIdtWhenUserIdNotSent() throws Exception {
        RequestBuilder request = get("/requests")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestClient);
    }

    @Test
    @DisplayName("getAllItemsRequests должна вернуть все запросы других пользователей")
    void shouldGetAllRequestsForOtherUserId() throws Exception {
        when(requestClient.getAllForOtherUsers(2))
                .thenReturn(ResponseEntity.ok().body(requests));

        RequestBuilder request = get("/requests/all")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.USER_ID_HEADER, 2L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].description", is(requests.get(0).description())))
                .andExpect(jsonPath("$[1].description", is(requests.get(1).description())));

        verify(requestClient, times(1)).getAllForOtherUsers(2);
        verifyNoMoreInteractions(requestClient);
    }

    @Test
    @DisplayName("getAllItemsRequests не должна возвращать данные когда не указан id пользователя")
    void shouldNotGetAllRequestsForOtherUserIdtWhenUserIdNotSent() throws Exception {
        RequestBuilder request = get("/requests/all")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestClient);
    }

    @Test
    @DisplayName("getItemRequestById должна вернуть запрос по указанному id")
    void shouldGetRequestById() throws Exception {
        when(requestClient.getById(2, 3))
                .thenReturn(ResponseEntity.ok().body(requests.getFirst()));

        RequestBuilder request = get("/requests/3")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.USER_ID_HEADER, 2L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.description", is(requests.getFirst().description())));

        verify(requestClient, times(1)).getById(2, 3);
        verifyNoMoreInteractions(requestClient);
    }

    @Test
    @DisplayName("getItemRequestById не должна возвращать данные когда не указан id пользователя")
    void shouldNotGetRequestByIdtWhenUserIdNotSent() throws Exception {
        RequestBuilder request = get("/requests/3")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestClient);
    }
}