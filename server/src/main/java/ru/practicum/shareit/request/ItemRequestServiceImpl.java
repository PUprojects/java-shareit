package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.RequestAnswerDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto add(long userId, NewItemRequestDto newItemRequestDto) {
        User requester = userService.getUserById(userId);

        ItemRequest newItemRequest = new ItemRequest();
        newItemRequest.setDescription(newItemRequestDto.description());
        newItemRequest.setRequester(requester);
        newItemRequest.setCreated(LocalDateTime.now());

        return itemRequestMapper.toItemRequestDto(itemRequestRepository.save(newItemRequest));
    }

    @Override
    public List<ItemRequestWithAnswersDto> getAllForUser(long userId) {
        List<ItemRequest> requests = itemRequestRepository.findByRequester_id(userId);

        return requests.stream()
                .map(itemRequest -> {
                    List<RequestAnswerDto> requestAnswers = itemRepository.findByRequest_id(itemRequest.getId());
                    return itemRequestMapper.toItemRequestWithAnswersDto(itemRequest, requestAnswers);
                })
                .toList();
    }

    @Override
    public List<ItemRequestDto> getAllForOtherUsers(long userId) {
        List<ItemRequest> requests = itemRequestRepository.findByRequester_idNot(userId);

        return requests.stream()
                .map(itemRequestMapper::toItemRequestDto)
                .toList();
    }

    @Override
    public ItemRequestWithAnswersDto getById(long requestId) {
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос c id = " + requestId + " не найден"));
        List<RequestAnswerDto> requestAnswers = itemRepository.findByRequest_id(request.getId());
        return itemRequestMapper.toItemRequestWithAnswersDto(request, requestAnswers);
    }

}
