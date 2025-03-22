package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @AllArgsConstructor
    static class RequestAnswerDtoTestImpl implements RequestAnswerDto {
        private long id;
        private String name;
        private long ownerId;

        @Override
        public long getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public long getOwnerId() {
            return ownerId;
        }
    }

    @Mock
    ItemRequestRepository itemRequestRepository;

    @Mock
    UserService userService;

    @Mock
    ItemRequestMapper itemRequestMapper;

    @Mock
    ItemRepository itemRepository;

    @InjectMocks
    ItemRequestServiceImpl itemRequestService;

    @Test
    @DisplayName("Должен создавать новый запрос")
    void shouldCreateNewItemRequest() {
        User requester = new User(1, "Test user 1", "user@mail.ru");

        when(userService.getUserById(Mockito.anyLong()))
                .thenReturn(requester);

        when(itemRequestRepository.save(any(ItemRequest.class)))
                .thenAnswer(argument -> {
                    ItemRequest itemRequest = argument.getArgument(0, ItemRequest.class);
                    itemRequest.setId(1);
                    return itemRequest;
                });

        when(itemRequestMapper.toItemRequestDto(any(ItemRequest.class)))
                .thenAnswer(argument -> {
                    ItemRequest itemRequest = argument.getArgument(0, ItemRequest.class);
                   return new ItemRequestDto(itemRequest.getId(), itemRequest.getDescription(),
                           itemRequest.getCreated());
                });

        NewItemRequestDto newItemRequestDto = new NewItemRequestDto("New request");

        ItemRequestDto itemRequestDto = itemRequestService.add(1, newItemRequestDto);

        assertNotNull(itemRequestDto);
        assertEquals(1, itemRequestDto.id());
        assertEquals("New request", itemRequestDto.description());
        assertNotNull(itemRequestDto.created());

        verify(userService, times(1))
                .getUserById(1);
        verify(itemRequestRepository, times(1))
                .save(any(ItemRequest.class));

        Mockito.verifyNoMoreInteractions(itemRequestRepository, userService);
    }

    @Test
    @DisplayName("Не должен создавать новый запрос при неверно указанном пользователе")
    void shouldNotCreateItemRequestWhenUserInvalid() {
        when(userService.getUserById(Mockito.anyLong()))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        NewItemRequestDto newItemRequestDto = new NewItemRequestDto("New request");

        assertThrows(NotFoundException.class, () -> itemRequestService.add(1,newItemRequestDto));
        Mockito.verifyNoInteractions(itemRequestRepository);
    }

    @Test
    @DisplayName("Должен вернуть все запросы для конкретного пользователя")
    void shouldReturnAllItemRequestsFormUser() {
        User requester = new User(1, "Test user 1", "user@mail.ru");
        LocalDateTime requestTime = LocalDateTime.now();
        List<ItemRequest> itemRequests = List.of(
                new ItemRequest(1, "First request", requestTime.minusHours(2), requester),
                new ItemRequest(2, "Second request", requestTime.minusHours(1), requester)
        );

        when(itemRequestRepository.findByRequester_id(Mockito.anyLong()))
                .thenReturn(itemRequests);

        when(itemRequestMapper.toItemRequestWithAnswersDto(any(ItemRequest.class), anyList()))
                .thenAnswer(arguments -> {
                    ItemRequest request = arguments.getArgument(0, ItemRequest.class);
                    List<RequestAnswerDto> items = arguments.getArgument(1);

                    return new ItemRequestWithAnswersDto(request.getId(), request.getDescription(),
                            request.getCreated(), items);
                });

        when(itemRepository.findByRequest_id(1))
                .thenReturn(List.of(new RequestAnswerDtoTestImpl(1, "Item 1", 5)));
        when(itemRepository.findByRequest_id(2)).thenReturn(List.of());

        List<ItemRequestWithAnswersDto> serviceAnswer = itemRequestService.getAllForUser(1);

        assertEquals(2, serviceAnswer.size());
        assertEquals("First request", serviceAnswer.get(0).description());
        assertEquals("Second request", serviceAnswer.get(1).description());
        assertEquals(1, serviceAnswer.get(0).items().size());
        assertEquals(0, serviceAnswer.get(1).items().size());
    }

    @Test
    @DisplayName("Должен вернуть все запросы других пользователей")
    void shouldReturnAllItemRequestsOtherUsers() {
        User requester = new User(1, "Test user 1", "user@mail.ru");
        LocalDateTime requestTime = LocalDateTime.now();
        List<ItemRequest> itemRequests = List.of(
                new ItemRequest(1, "First request", requestTime.minusHours(2), requester),
                new ItemRequest(2, "Second request", requestTime.minusHours(1), requester)
        );

        when(itemRequestRepository.findByRequester_idNot(Mockito.anyLong()))
                .thenReturn(itemRequests);

        when(itemRequestMapper.toItemRequestDto(any(ItemRequest.class)))
                .thenAnswer(argument -> {
                    ItemRequest itemRequest = argument.getArgument(0, ItemRequest.class);
                    return new ItemRequestDto(itemRequest.getId(), itemRequest.getDescription(),
                            itemRequest.getCreated());
                });

        List<ItemRequestDto> serviceAnswer = itemRequestService.getAllForOtherUsers(5);

        assertEquals(2, serviceAnswer.size());
        assertEquals("First request", serviceAnswer.get(0).description());
        assertEquals("Second request", serviceAnswer.get(1).description());
    }

    @Test
    @DisplayName("Должен вернуть все запрос по указанному идентификатору")
    void shouldReturnItemRequestById() {
        User requester = new User(1, "Test user 1", "user@mail.ru");

        when(itemRequestRepository.findById(1L))
                .thenReturn(Optional.of(new ItemRequest(1, "First request", LocalDateTime.now(), requester)));

        when(itemRequestMapper.toItemRequestWithAnswersDto(any(ItemRequest.class), anyList()))
                .thenAnswer(arguments -> {
                    ItemRequest request = arguments.getArgument(0, ItemRequest.class);
                    List<RequestAnswerDto> items = arguments.getArgument(1);

                    return new ItemRequestWithAnswersDto(request.getId(), request.getDescription(),
                            request.getCreated(), items);
                });

        when(itemRepository.findByRequest_id(1))
                .thenReturn(List.of(new RequestAnswerDtoTestImpl(1, "Item 1", 5)));

        ItemRequestWithAnswersDto serviceAnswer = itemRequestService.getById(1);

        assertEquals(1, serviceAnswer.id());
        assertEquals("First request", serviceAnswer.description());
        assertEquals(1, serviceAnswer.items().size());
        assertEquals("Item 1", serviceAnswer.items().getFirst().getName());
    }

    @Test
    void getByIdShouldThrowExceptionWhenRequestNotFound() {
        when(itemRequestRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getById(5L));

        verify(itemRequestRepository, times(1)).findById(5L);
        verifyNoMoreInteractions(itemRequestRepository);
    }
}