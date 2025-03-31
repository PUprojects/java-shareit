package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingAndCommentsDto;
import ru.practicum.shareit.item.dto.NewCommentDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {
    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    private final ItemDto itemDto = new ItemDto(1L, "Item", "ItemDesc", true, null);

    @Test
    void createShouldCreateItemAndReturnDtoWhenInvoked() {
        when(itemService.create(2L, itemDto)).thenReturn(itemDto);

        ItemDto createdDto = itemController.create(itemDto, 2L);

        assertEquals(itemDto, createdDto, "Возвращённые данные не соответсвуют ожидаемым");
        verify(itemService, times(1)).create(2L, itemDto);
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void updateShouldUpdateItemAndReturnDtoWhenInvoked() {
        when(itemService.update(3L, 1L, itemDto)).thenReturn(itemDto);

        ItemDto updatedDto = itemController.update(itemDto, 1L, 3L);

        assertEquals(itemDto, updatedDto, "Возвращённые данные не соответсвуют ожидаемым");
        verify(itemService, times(1)).update(3L, 1L, itemDto);
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void getShouldReturnDtoWithBookingAndCommentsWhenInvoked() {
        ItemWithBookingAndCommentsDto expectedDto = new ItemWithBookingAndCommentsDto(2L, "Item",
                "Description", true, 6L, null, null, null);
        when(itemService.getByIdWithBookingsAndComments(2L)).thenReturn(expectedDto);

        ItemWithBookingAndCommentsDto resultDto = itemController.get(2L, 1L);

        assertEquals(expectedDto, resultDto, "Возвращённые данные не соответсвуют ожидаемым");
        verify(itemService, times(1)).getByIdWithBookingsAndComments(2L);
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void getAllByUserShouldReturnUsersItemsListWhenInvoked() {
        List<ItemDto> expectedList = List.of(itemDto);
        when(itemService.getAllByUser(1L)).thenReturn(expectedList);

        List<ItemDto> resultList = itemController.getAllByUser(1L);

        assertEquals(expectedList, resultList, "Возвращённые данные не соответсвуют ожидаемым");
        verify(itemService, times(1)).getAllByUser(1L);
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void searchItemsForBookingShouldReturnFoundedItemsListWhenInvoked() {
        List<ItemDto> expectedList = List.of(itemDto);
        when(itemService.searchAvailable("search text")).thenReturn(expectedList);

        List<ItemDto> resultList = itemController.searchItemsForBooking(1L, "search text");

        assertEquals(expectedList, resultList, "Возвращённые данные не соответсвуют ожидаемым");
        verify(itemService, times(1)).searchAvailable("search text");
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void addCommentShouldAddCommentAndReturnDtoWhenInvoked() {
        NewCommentDto newCommentDto = new NewCommentDto("Comment text");
        CommentDto expectedDto = new CommentDto(11L, "Comment text", 1L, "User",
                LocalDateTime.of(2025, 1, 22, 17, 42));

        when(itemService.addComment(1L,2L,"Comment text")).thenReturn(expectedDto);

        CommentDto resultDto = itemController.addComment(1L, newCommentDto, 2L);

        assertEquals(expectedDto, resultDto, "Возвращённые данные не соответсвуют ожидаемым");
        verify(itemService, times(1))
                .addComment(1L,2L,"Comment text");
        verifyNoMoreInteractions(itemService);
    }
}