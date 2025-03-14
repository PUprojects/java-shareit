package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constants.AppConstants;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public ItemRequestDto addNewRequest(@RequestBody NewItemRequestDto newItemRequestDto,
                                        @RequestHeader(AppConstants.UserIdHeader) long userId) {
        log.info("От пользователя {} получен запрос POST /requests: {}", userId, newItemRequestDto);
        ItemRequestDto result = itemRequestService.add(userId, newItemRequestDto);
        log.info("Получен результат {}", result);
        return result;
    }

    @GetMapping
    public List<ItemRequestWithAnswersDto> getUserRequests(
            @RequestHeader(AppConstants.UserIdHeader) long userId) {
        log.info("От пользователя {} получен запрос GET /requests", userId);
        return itemRequestService.getAllForUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllItemsRequests(@RequestHeader(AppConstants.UserIdHeader) long userId) {
        log.info("От пользователя {} получен запрос GET /requests/all", userId);
        return itemRequestService.getAllForOtherUsers(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithAnswersDto getItemRequestById(@PathVariable long requestId) {
        log.info("От получен запрос GET /requests/{}", requestId);
        return itemRequestService.getById(requestId);
    }
}
