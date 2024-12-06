package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public List<ItemDto> getAllByUser(long userId) {
        return itemRepository.getAllByUser(userId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public ItemDto getById(long id) {
        return ItemMapper.toItemDto(itemRepository.getById(id)
                .orElseThrow(() -> new NotFoundException("Вещь " + id + " не найдена")));
    }

    @Override
    public ItemDto create(long ownerId, ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        User user = userService.getUserById(ownerId);
        item.setOwner(user);
        return ItemMapper.toItemDto(itemRepository.create(item));
    }

    @Override
    public ItemDto update(long userId, long itemId, ItemDto itemDto) {
        Item item = getItemById(itemId);

        if (item.getOwner().getId() != userId) {
            throw new AccessDeniedException("Пользователь " + userId + " не является хозяином вещи");
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        itemRepository.update(item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public void delete(long id) {

    }

    @Override
    public List<ItemDto> searchAvailable(String searchString) {
        if (searchString.isEmpty())
            return List.of();
        return itemRepository.search(searchString, true).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    private Item getItemById(long id) {
        return itemRepository.getById(id)
                .orElseThrow(() -> new NotFoundException("Вещь " + id + " не найдена"));
    }
}
