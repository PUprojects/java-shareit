package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ItemRepositoryInMemory implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private long newItemId = 1;

    @Override
    public List<Item> getAllByUser(long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId() == userId)
                .toList();
    }

    @Override
    public Optional<Item> getById(long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public Item create(Item item) {
        item.setId(newItemId++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public void update(Item item) {
        items.put(item.getId(), item);
    }

    @Override
    public void delete(Item item) {
        items.remove(item.getId());
    }

    @Override
    public List<Item> search(String text, boolean isAvailable) {
        String textToSearch = text.toLowerCase();
        return items.values().stream()
                .filter(item -> (item.isAvailable() == isAvailable)
                            && (item.getName().toLowerCase().contains(textToSearch)
                                || item.getDescription().toLowerCase().contains(textToSearch)))
                .toList();
    }
}
