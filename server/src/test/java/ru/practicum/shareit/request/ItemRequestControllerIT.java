package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerIT {
    @MockitoBean
    ItemRequestServiceImpl itemRequestService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    MockMvc mvc;

    private final List<ItemRequestDto>  itemRequestDtos = List.of(
            new ItemRequestDto(1L,"Test item request",
                    LocalDateTime.of(2025, 1, 10, 12, 0)),
            new ItemRequestDto(2L,"Second Test item request",
                    LocalDateTime.of(2025, 1, 11, 13, 10))
        );

    private final List<ItemRequestWithAnswersDto> itemRequestWithAnswersDtos = List.of(
            new ItemRequestWithAnswersDto(1L, "First Request",
                    LocalDateTime.of(2025, 1, 10, 12, 0), List.of())
    );


    @Test
    @DisplayName("Должен создавать новый запрос вещи")
    void shouldCreateNewItemRequest() throws Exception {

        NewItemRequestDto newItemRequestDto = new NewItemRequestDto(itemRequestDtos.getFirst().description());

        when(itemRequestService.add(anyLong(), any()))
                .thenReturn(itemRequestDtos.getFirst());

        RequestBuilder request = post("/requests")
                .header(AppConstants.UserIdHeader, 22L)
                .content(mapper.writeValueAsString(newItemRequestDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(itemRequestDtos.getFirst().id()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDtos.getFirst().description())))
                .andExpect(jsonPath("$.created", is(itemRequestDtos.getFirst().created()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        verify(itemRequestService, times(1)).add(anyLong(), any());
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    @DisplayName("getUserRequests должна вернуть все запросы указанного пользователя")
    void shouldGetAllRequestsByUserId() throws Exception {
        when(itemRequestService.getAllForUser(2))
                .thenReturn(itemRequestWithAnswersDtos);

        RequestBuilder request = get("/requests")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 2L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id", is(itemRequestWithAnswersDtos.getFirst().id()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestWithAnswersDtos.getFirst().description())))
                .andExpect(jsonPath("$[0].created", is(itemRequestWithAnswersDtos.getFirst().created()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));


        verify(itemRequestService, times(1)).getAllForUser(2);
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    @DisplayName("getAllItemsRequests должна вернуть все запросы других пользователей")
    void shouldGetAllRequestsForOtherUserId() throws Exception {

        when(itemRequestService.getAllForOtherUsers(2))
                .thenReturn(itemRequestDtos);

        RequestBuilder request = get("/requests/all")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 2L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].description", is(itemRequestDtos.get(0).description())))
                .andExpect(jsonPath("$[0].description", is(itemRequestDtos.get(0).description())))
                .andExpect(jsonPath("$[0].created", is(itemRequestDtos.get(0).created()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[1].description", is(itemRequestDtos.get(1).description())))
                .andExpect(jsonPath("$[1].description", is(itemRequestDtos.get(1).description())))
                .andExpect(jsonPath("$[1].created", is(itemRequestDtos.get(1).created()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        verify(itemRequestService, times(1)).getAllForOtherUsers(2);
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    @DisplayName("getItemRequestById должна вернуть запрос по указанному id")
    void shouldGetRequestById() throws Exception {
        when(itemRequestService.getById(3))
                .thenReturn(itemRequestWithAnswersDtos.getFirst());

        RequestBuilder request = get("/requests/3")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 2L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(itemRequestWithAnswersDtos.getFirst().id()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestWithAnswersDtos.getFirst().description())))
                .andExpect(jsonPath("$.created", is(itemRequestWithAnswersDtos.getFirst().created()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        verify(itemRequestService, times(1)).getById(3);
        verifyNoMoreInteractions(itemRequestService);
    }
}