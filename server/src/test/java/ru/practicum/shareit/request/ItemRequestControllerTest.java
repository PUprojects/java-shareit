package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {
    @Mock
    private ItemRequestService itemRequestService;

    @InjectMocks
    private ItemRequestController itemRequestController;

    @Test
    void addNewRequestShouldAddNewRequest() {
        NewItemRequestDto newItemRequestDto = new NewItemRequestDto("Request");
        ItemRequestDto requestDto = new ItemRequestDto(1, "Request",
                LocalDateTime.of(2025,3,15,12,34));
        when(itemRequestService.add(1, newItemRequestDto)).thenReturn(requestDto);

        ItemRequestDto newRequest =
                itemRequestController.addNewRequest(newItemRequestDto,1L);

        assertEquals(requestDto, newRequest, "Возвращённые данные не соответсвуют ожидаемым");
        verify(itemRequestService, times(1)).add(1L, newItemRequestDto);
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void getUserRequestsShouldReturnDataWhenInvoked() {
        List<ItemRequestWithAnswersDto> expectedData =
                List.of(new ItemRequestWithAnswersDto(1, "Request",
                        LocalDateTime.of(2025,3,15,12,34),
                        List.of()));
        when(itemRequestService.getAllForUser(2)).thenReturn(expectedData);

        List<ItemRequestWithAnswersDto> result = itemRequestController.getUserRequests(2);

        assertEquals(expectedData.getFirst(), result.getFirst(),
                "Возвращённые данные не соответсвуют ожидаемым");
        verify(itemRequestService, times(1)).getAllForUser(2);
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void getAllItemsRequestsShouldReturnDataWhenInvoked() {
        List<ItemRequestDto> expectedData = List.of(new ItemRequestDto(1, "Request",
                LocalDateTime.of(2025,3,15,12,34)));
        when(itemRequestService.getAllForOtherUsers(3)).thenReturn(expectedData);

        List<ItemRequestDto> result = itemRequestController.getAllItemsRequests(3L);

        assertEquals(expectedData.getFirst(), result.getFirst(),
                "Возвращённые данные не соответсвуют ожидаемым");
        verify(itemRequestService, times(1)).getAllForOtherUsers(3);
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void getItemRequestByIdShouldReturnDataWhenInvoked() {
        ItemRequestWithAnswersDto expectedData = new ItemRequestWithAnswersDto(1, "Request",
                    LocalDateTime.of(2025,3,15,12,34),
                    List.of());
        when(itemRequestService.getById(55)).thenReturn(expectedData);

        ItemRequestWithAnswersDto result = itemRequestController.getItemRequestById(55);

        assertEquals(expectedData, result, "Возвращённые данные не соответсвуют ожидаемым");
        verify(itemRequestService, times(1)).getById(55);
        verifyNoMoreInteractions(itemRequestService);
    }
}