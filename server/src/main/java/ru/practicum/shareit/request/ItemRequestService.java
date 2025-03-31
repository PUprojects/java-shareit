package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto add(long userId, NewItemRequestDto newItemRequestDto);

    List<ItemRequestWithAnswersDto> getAllForUser(long userId);

    List<ItemRequestDto> getAllForOtherUsers(long userId);

    ItemRequestWithAnswersDto getById(long requestId);
}
