package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingAndCommentsDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    List<ItemDto> getAllByUser(long userId);

    ItemWithBookingAndCommentsDto getByIdWithBookingsAndComments(long id);

    ItemDto getById(long id);

    ItemDto create(long ownerId, ItemDto itemDto);

    ItemDto update(long userId, long itemId, ItemDto itemDto);

    void delete(long id);

    List<ItemDto> searchAvailable(String searchString);

    Item getItemById(long id);

    CommentDto addComment(long itemId, long authorId, String text);
}
