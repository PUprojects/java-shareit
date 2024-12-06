package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    List<Item> getAllByUser(long userId);

    Optional<Item> getById(long itemId);

    Item create(Item item);

    void update(Item item);

    void delete(Item item);

    List<Item> search(String text, boolean isAvailable);
}
