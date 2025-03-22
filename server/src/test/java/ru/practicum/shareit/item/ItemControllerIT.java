package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingAndCommentsDto;
import ru.practicum.shareit.item.dto.NewCommentDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerIT {
    @MockitoBean
    ItemServiceImpl itemService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    MockMvc mvc;

    private final List<ItemDto> items = List.of(
            new ItemDto(1L, "First item", "First item desc", true, null),
            new ItemDto(2L, "Second item", "Second item desc", false, null),
            new ItemDto(3L, "Third item", "Third item desc", true, 5L)
    );

    @Test
    @DisplayName("create должна создавать вещь")
    void shouldCreateItem() throws Exception {
        when(itemService.create(anyLong(), any(ItemDto.class)))
                .thenReturn(items.getFirst());

        RequestBuilder request = post("/items")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.USER_ID_HEADER, 1L)
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

        verify(itemService, times(1)).create(anyLong(), any(ItemDto.class));
        verifyNoMoreInteractions(itemService);
    }

    @Test
    @DisplayName("update должна обновлять состояние вещи")
    void shouldUpdateItem() throws Exception {
        when(itemService.update(anyLong(), anyLong(), any(ItemDto.class)))
                .thenReturn(items.getFirst());

        RequestBuilder request = patch("/items/1")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.USER_ID_HEADER, 1L)
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

        verify(itemService, times(1)).update(anyLong(), anyLong(), any(ItemDto.class));
        verifyNoMoreInteractions(itemService);
    }

    @Test
    @DisplayName("get должна возвращать информацию о вещи")
    void shouldGetItem() throws Exception {
        ItemWithBookingAndCommentsDto item = new ItemWithBookingAndCommentsDto(2L, "Item",
                "Description", true, 6L, null, null, null);

        when(itemService.getByIdWithBookingsAndComments(2))
                .thenReturn(item);

        RequestBuilder request = get("/items/2")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.USER_ID_HEADER, 1L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("Item"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.requestId").value(6L))
                .andExpect(jsonPath("$.lastBooking").isEmpty())
                .andExpect(jsonPath("$.nextBooking").isEmpty())
                .andExpect(jsonPath("$.comments").isEmpty());

        verify(itemService, times(1)).getByIdWithBookingsAndComments(2);
        verifyNoMoreInteractions(itemService);
    }

    @Test
    @DisplayName("getAllByUser должна возвращать все вещи указанного полльзователя")
    void shouldGetAllItemsByUserId() throws Exception {
        when(itemService.getAllByUser(1))
                .thenReturn(items);

        RequestBuilder request = get("/items")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.USER_ID_HEADER, 1L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(items.get(0).id()))
                .andExpect(jsonPath("$[0].name").value(items.get(0).name()))
                .andExpect(jsonPath("$[0].description").value(items.get(0).description()))
                .andExpect(jsonPath("$[0].available").value(items.get(0).available()))
                .andExpect(jsonPath("$[0].requestId").isEmpty())
                .andExpect(jsonPath("$[1].id").value(items.get(1).id()))
                .andExpect(jsonPath("$[1].name").value(items.get(1).name()))
                .andExpect(jsonPath("$[1].description").value(items.get(1).description()))
                .andExpect(jsonPath("$[1].available").value(items.get(1).available()))
                .andExpect(jsonPath("$[1].requestId").isEmpty())
                .andExpect(jsonPath("$[2].id").value(items.get(2).id()))
                .andExpect(jsonPath("$[2].name").value(items.get(2).name()))
                .andExpect(jsonPath("$[2].description").value(items.get(2).description()))
                .andExpect(jsonPath("$[2].available").value(items.get(2).available()))
                .andExpect(jsonPath("$[2].requestId").value(items.get(2).requestId()));

        verify(itemService, times(1)).getAllByUser(1);
        verifyNoMoreInteractions(itemService);
    }

    @Test
    @DisplayName("addComment должна создавать комментарий")
    void shouldCreateComment() throws Exception {
        NewCommentDto newCommentDto = new NewCommentDto("Comment text");
        CommentDto commentDto = new CommentDto(11L, "Comment text", 1L, "User",
                LocalDateTime.of(2025, 1, 22, 17, 42));

        when(itemService.addComment(anyLong(), anyLong(), any(String.class)))
                .thenReturn(commentDto);

        RequestBuilder request = post("/items/1/comment")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.USER_ID_HEADER, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(newCommentDto));

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(commentDto.id()))
                .andExpect(jsonPath("$.text").value(commentDto.text()))
                .andExpect(jsonPath("$.itemId").value(commentDto.itemId()))
                .andExpect(jsonPath("$.authorName").value(commentDto.authorName()))
                .andExpect(jsonPath("$.created").value(commentDto.created()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

        verify(itemService, times(1)).addComment(anyLong(), anyLong(), any(String.class));
        verifyNoMoreInteractions(itemService);
    }
}