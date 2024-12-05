package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getAllByUser(long userId);

    ItemDto getById(long id);

    ItemDto create(long ownerId, ItemDto itemDto);

    ItemDto update(long userId, long itemId, ItemDto itemDto);

    void delete(long id);

    List<ItemDto> searchAvailable(String searchString);
}
