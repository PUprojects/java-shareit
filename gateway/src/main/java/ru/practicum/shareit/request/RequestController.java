package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import ru.practicum.shareit.constants.AppConstants;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {
    private final RequestClient requestClient;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public ResponseEntity<Object> addNewRequest(@Valid @RequestBody ItemRequestDto newItemRequestDto,
                                                @RequestHeader(AppConstants.USER_ID_HEADER) long userId) {
        log.info("От пользователя {} получен запрос POST /requests: {}", userId, newItemRequestDto);
        return requestClient.add(userId, newItemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getUserRequests(
            @RequestHeader(AppConstants.USER_ID_HEADER) long userId) {
        log.info("От пользователя {} получен запрос GET /requests", userId);
        return requestClient.getAllForUser(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemsRequests(@RequestHeader(AppConstants.USER_ID_HEADER) long userId) {
        log.info("От пользователя {} получен запрос GET /requests/all", userId);
        return requestClient.getAllForOtherUsers(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestById(@RequestHeader(AppConstants.USER_ID_HEADER) long userId,
                                                     @PathVariable long requestId) {
        log.info("От получен запрос GET /requests/{}", requestId);
        return requestClient.getById(userId, requestId);
    }

}
