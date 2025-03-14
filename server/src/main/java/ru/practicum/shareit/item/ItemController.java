package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constants.AppConstants;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingAndCommentsDto;
import ru.practicum.shareit.item.dto.NewCommentDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestBody ItemDto itemDto,
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
    public ItemWithBookingAndCommentsDto get(@PathVariable long itemId,
                                             @RequestHeader(AppConstants.UserIdHeader) long userId) {
        log.info("От пользователя {} получен запрос GET /items/{}", userId, itemId);
        return itemService.getByIdWithBookingsAndComments(itemId);
    }

    @GetMapping
    public List<ItemDto> getAllByUser(@RequestHeader(AppConstants.UserIdHeader) long userId) {
        log.info("От пользователя {} получен запрос GET /items", userId);
        return itemService.getAllByUser(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItemsForBooking(@RequestHeader(AppConstants.UserIdHeader) long userId,
                                               @RequestParam("text") String text) {
        log.info("От пользователя {} получен запрос GET /items/search с параметром поиска = {}", userId, text);
        return itemService.searchAvailable(text);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable long itemId,
                                 @RequestBody NewCommentDto comment,
                                 @RequestHeader(AppConstants.UserIdHeader) long userId) {
        log.info("От пользователя {} получен запрос POST /items/{}/comment: {}", userId, itemId, comment);

        return itemService.addComment(itemId, userId, comment.text());
    }
}

