package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constants.AppConstants;
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
                          @RequestHeader(AppConstants.UserIdHeader) long userId) {
        log.info("От пользователя {} получен запрос POST /items: {}", userId, itemDto);
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody ItemDto itemDto,
                          @PathVariable long itemId,
                          @RequestHeader(AppConstants.UserIdHeader) long userId) {
        log.info("От пользователя {} получен запрос PATCH /items/{}: {}", userId, itemId, itemDto);
        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto get(@PathVariable long itemId,
                       @RequestHeader(AppConstants.UserIdHeader) long userId) {
        log.info("От пользователя {} получен запрос GET /items/{}", userId, itemId);
        return itemService.getById(itemId);
    }

    @GetMapping
    public List<ItemDto> getAllByUser(@RequestHeader(AppConstants.UserIdHeader) long userId) {
        log.info("От пользователя {} получен запрос GET /items", userId);
        return itemService.getAllByUser(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItemsForBooking(@RequestHeader(AppConstants.UserIdHeader) long userId,
                                               @RequestParam("text") String text) {
        log.info("От  пользователя {} получен запрос GET /items/search с параметром поиска = {}", userId, text);
        return itemService.searchAvailable(text);
    }
}

