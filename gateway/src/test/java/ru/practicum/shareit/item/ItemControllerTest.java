package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import ru.practicum.shareit.constants.AppConstants;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    static class BadItemsArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of(new ItemDto(1L, "", "First item desc", true, null)),
                    Arguments.of(new ItemDto(1L, "First item", "", true, null)),
                    Arguments.of(new ItemDto(1L, "First item", "First item desc", null, null)));
        }
    }

    @Autowired
    ObjectMapper mapper;

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ItemClient itemClient;

    private final List<ItemDto> items = List.of(
            new ItemDto(1L, "First item", "First item desc", true, null),
            new ItemDto(2L, "Second item", "Second item desc", false, null),
            new ItemDto(3L, "Third item", "Third item desc", true, 5L)
    );

    @Test
    @DisplayName("create должна создавать вещь")
    void shouldCreateItem() throws Exception {
        when(itemClient.create(anyLong(), any(ItemDto.class)))
                .thenReturn(ResponseEntity.created(URI.create("/items/1")).body(items.getFirst()));

        RequestBuilder request = post("/items")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(items.getFirst()));

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("First item"))
                .andExpect(jsonPath("$.description").value("First item desc"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.requestId").isEmpty());

        verify(itemClient, times(1)).create(anyLong(), any(ItemDto.class));
        verifyNoMoreInteractions(itemClient);
    }

    @ParameterizedTest(name = "Запрос {index}")
    @DisplayName("create не должна создавать вещь при некорректном запросе")
    @ArgumentsSource(BadItemsArgumentsProvider.class)
    void shouldNotCreateItemWhenInvalidRequest(ItemDto itemDto) throws Exception {
        RequestBuilder request = post("/items")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(itemDto));

        mvc.perform(request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    @DisplayName("update должна обновлять состояние вещи")
    void shouldUpdateItem() throws Exception {
        when(itemClient.update(anyLong(), anyLong(), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok().body(items.getFirst()));

        RequestBuilder request = patch("/items/1")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(items.getFirst()));

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("First item"))
                .andExpect(jsonPath("$.description").value("First item desc"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.requestId").isEmpty());

        verify(itemClient, times(1)).update(anyLong(), anyLong(), any(ItemDto.class));
        verifyNoMoreInteractions(itemClient);
    }

    @ParameterizedTest(name = "Запрос {index}")
    @DisplayName("update не должна обновлять состояние вещи при некорректном запросе")
    @ArgumentsSource(BadItemsArgumentsProvider.class)
    void shouldNotUpdateItemWhenInvalidRequest(ItemDto itemDto) throws Exception {
        RequestBuilder request = patch("/items/1")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(itemDto));

        mvc.perform(request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    @DisplayName("get должна возвращать информацию о вещи")
    void shouldGetItem() throws Exception {
        when(itemClient.getByIdWithBookingsAndComments(1,2))
                .thenReturn(ResponseEntity.ok().body(items.get(1)));

        RequestBuilder request = get("/items/2")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 1L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id").value(2L));

        verify(itemClient, times(1)).getByIdWithBookingsAndComments(1,2);
        verifyNoMoreInteractions(itemClient);
    }

    @Test
    @DisplayName("get не должна возвращать информацию о вещи, если не указан id пользователя")
    void shouldNotGetItemWhenNoUserId() throws Exception {
        RequestBuilder request = get("/items/5")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    @DisplayName("getAllByUser должна возвращать все вещи указанного полльзователя")
    void shouldGetAllItemsByUserId() throws Exception {
        when(itemClient.getAllByUser(1))
                .thenReturn(ResponseEntity.ok().body(items));

        RequestBuilder request = get("/items")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 1L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[2].id").value(3L));

        verify(itemClient, times(1)).getAllByUser(1);
        verifyNoMoreInteractions(itemClient);
    }

    @Test
    @DisplayName("getAllByUser не должна возвращать данные, если не указан id пользователя")
    void shouldNotGetItemsWhenNoUserId() throws Exception {
        RequestBuilder request = get("/items")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    @DisplayName("addComment должна создавать комментарий")
    void shouldCreateComment() throws Exception {
        CommentDto commentDto = new CommentDto("New comment");

        when(itemClient.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenReturn(ResponseEntity.created(URI.create("/comments/5")).body(commentDto));

        RequestBuilder request = post("/items/1/comment")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentDto));

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.text").value(commentDto.text()));

        verify(itemClient, times(1)).addComment(anyLong(), anyLong(), any(CommentDto.class));
        verifyNoMoreInteractions(itemClient);
    }

    @Test
    @DisplayName("addComment не должна создавать пустой комментарий")
    void shouldNotCreateEmptyComment() throws Exception {
        CommentDto commentDto = new CommentDto("");

        RequestBuilder request = post("/items/1/comment")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentDto));

        mvc.perform(request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    @DisplayName("addComment не должна создавать длинный комментарий")
    void shouldNotCreateLongComment() throws Exception {
        CommentDto commentDto = new CommentDto(Strings.repeat("X", 256));

        RequestBuilder request = post("/items/1/comment")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(commentDto));

        mvc.perform(request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }
}