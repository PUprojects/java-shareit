package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingAndCommentsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {
    private final ItemMapper itemMapper = new ItemMapperImpl();

    private final User user = new User(1, "First user", "first@mail.ru");
    private final User requester = new User(2, "Second user", "second@mail.ru");
    private final ItemRequest itemRequest = new ItemRequest(1, "Need item",
            LocalDateTime.now().minusHours(1), requester);
    private final Item item = new Item(1, "First item", "First item desc",
            true, user, itemRequest);
    private final Item itemNoRequest = new Item(1, "First item", "First item desc",
            true, user, null);


    @Test
    void toItemDtoShouldReturnNullWhenItemIsNull() {
        assertNull(itemMapper.toItemDto(null), "Возвращаемое значение должно быть null");
    }

    @Test
    void toItemShouldReturnNullWhenItemDtoIsNull() {
        assertNull(itemMapper.toItem(null), "Возвращаемое значение должно быть null");
    }

    @Test
    void toItemWithBookingAndCommentsDtoShouldReturnNullWhenAllInParametersIsNull() {
        assertNull(itemMapper.toItemWithBookingAndCommentsDto(null, null, null, null),
                "Возвращаемое значение должно быть null");
    }

    @Test
    void toItemDtoShouldMapItemToDtoWhenRequestIsNotNull() {
        ItemDto mappedItemDto = itemMapper.toItemDto(item);

        assertEquals(item.getId(), mappedItemDto.id(), "id не совпадает");
        assertEquals(item.getDescription(), mappedItemDto.description(), "Описание не совпадает");
        assertEquals(item.getName(), mappedItemDto.name(), "Имя не совпадает");
        assertEquals(item.isAvailable(), mappedItemDto.available(), "Доступность не совпадает");
        assertEquals(item.getRequest().getId(), mappedItemDto.requestId(), "id запроса не совпадает");
    }

    @Test
    void toItemDtoShouldMapItemToDtoWhenRequestIsNull() {
        ItemDto mappedItemDto = itemMapper.toItemDto(itemNoRequest);

        assertEquals(itemNoRequest.getId(), mappedItemDto.id(), "id не совпадает");
        assertEquals(itemNoRequest.getDescription(), mappedItemDto.description(), "Описание не совпадает");
        assertEquals(itemNoRequest.getName(), mappedItemDto.name(), "Имя не совпадает");
        assertEquals(itemNoRequest.isAvailable(), mappedItemDto.available(), "Доступность не совпадает");
        assertNull(mappedItemDto.requestId(), "id запроса должен быть null");
    }

    @Test
    void toItemShouldMapDtoToItem() {
        ItemDto itemDto = new ItemDto(1L, "First item", "First item desc",
                true, null);

        Item mappedItem = itemMapper.toItem(itemDto);

        assertEquals(mappedItem.getId(), itemDto.id(), "id не совпадает");
        assertEquals(mappedItem.getDescription(), itemDto.description(), "Описание не совпадает");
        assertEquals(mappedItem.getName(), itemDto.name(), "Имя не совпадает");
        assertEquals(mappedItem.isAvailable(), itemDto.available(), "Доступность не совпадает");
    }

    @Test
    void toItemWithBookingAndCommentsDtoShouldMapDataToDto() {
        BookingDto lastBooking =  new BookingDto(1, LocalDateTime.now().minusHours(1), LocalDateTime.now(),
                item, user, BookingStatus.APPROVED);
        BookingDto nextBooking =  new BookingDto(2, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
                item, user, BookingStatus.WAITING);
        List<CommentDto> comments = List.of(new CommentDto(1, "Comment text", 1, "User",
                LocalDateTime.now().minusMinutes(10)));

        ItemWithBookingAndCommentsDto mappedDto = itemMapper.toItemWithBookingAndCommentsDto(item, lastBooking,
                nextBooking, comments);

        assertEquals(item.getId(), mappedDto.id(), "id не совпадает");
        assertEquals(item.getDescription(), mappedDto.description(), "Описание не совпадает");
        assertEquals(item.getName(), mappedDto.name(), "Имя не совпадает");
        assertEquals(item.isAvailable(), mappedDto.available(), "Доступность не совпадает");
        assertEquals(item.getRequest().getId(), mappedDto.requestId(), "id запроса не совпадает");
        assertEquals(lastBooking, mappedDto.lastBooking(), "Прошлое бронирование не совпадает");
        assertEquals(nextBooking, mappedDto.nextBooking(), "Следующее бронирование не совпадает");
        assertEquals(comments.getFirst(), mappedDto.comments().getFirst(), "Комментарий не совпадает");
    }
}