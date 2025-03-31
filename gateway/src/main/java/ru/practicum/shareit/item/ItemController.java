package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constants.AppConstants;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@Valid @RequestBody ItemDto itemDto,
                                         @RequestHeader(AppConstants.USER_ID_HEADER) long userId) {
        log.info("От пользователя {} получен запрос POST /items: {}", userId, itemDto);
        return itemClient.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@Valid @RequestBody ItemDto itemDto,
                          @PathVariable long itemId,
                          @RequestHeader(AppConstants.USER_ID_HEADER) long userId) {
        log.info("От пользователя {} получен запрос PATCH /items/{}: {}", userId, itemId, itemDto);
        return itemClient.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> get(@PathVariable long itemId,
                                      @RequestHeader(AppConstants.USER_ID_HEADER) long userId) {
        log.info("От пользователя {} получен запрос GET /items/{}", userId, itemId);
        return itemClient.getByIdWithBookingsAndComments(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByUser(@RequestHeader(AppConstants.USER_ID_HEADER) long userId) {
        log.info("От пользователя {} получен запрос GET /items", userId);
        return itemClient.getAllByUser(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItemsForBooking(@RequestHeader(AppConstants.USER_ID_HEADER) long userId,
                                               @RequestParam("text") String text) {
        log.info("От пользователя {} получен запрос GET /items/search с параметром поиска = {}", userId, text);
        return itemClient.searchAvailable(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> addComment(@PathVariable long itemId,
                                             @Valid @RequestBody CommentDto comment,
                                             @RequestHeader(AppConstants.USER_ID_HEADER) long userId) {
        log.info("От пользователя {} получен запрос POST /items/{}/comment: {}", userId, itemId, comment);

        return itemClient.addComment(userId, itemId, comment);
    }

}
