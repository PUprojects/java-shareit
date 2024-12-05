package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestBody @Valid ItemDto itemDto,
                          @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("От пользователя {} получен запрос POST /items: {}", userId, itemDto);
        final ItemDto createdItemDto = itemService.create(userId, itemDto);
        log.info("Отправлен ответ POST /items: {}", createdItemDto);
        return createdItemDto;
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody ItemDto itemDto,
                          @PathVariable long itemId,
                          @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("От пользователя {} получен запрос PATCH /items/{}: {}", userId, itemId, itemDto);
        final ItemDto updatedItemDto = itemService.update(userId, itemId, itemDto);
        log.info("Отправлен ответ PATCH /items/{}: {}", itemId, updatedItemDto);
        return updatedItemDto;
    }

    @GetMapping("/{itemId}")
    public ItemDto get(@PathVariable long itemId,
                       @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("От пользователя {} получен запрос GET /items/{}", userId, itemId);
        final ItemDto itemDto = itemService.getById(itemId);
        log.info("Отправлен ответ GET /items/{}: {}", itemId, itemDto);
        return itemDto;
    }

    @GetMapping
    public List<ItemDto> getAllByUser(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("От пользователя {} получен запрос GET /items", userId);
        final List<ItemDto> itemDtoList = itemService.getAllByUser(userId);
        log.info("Отправлен ответ GET /items: {}", itemDtoList);
        return itemDtoList;
    }

    @GetMapping("/search")
    public List<ItemDto> searchItemsForBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                               @RequestParam("text") String text) {
        log.info("От  пользователя {} получен запрос GET /items/search с параметром поиска = {}", userId, text);
        List<ItemDto> itemDtoList = itemService.searchAvailable(text);
        log.info("Отправлен ответ GET /items/search: {}", itemDtoList);
        return itemDtoList;
    }
}

