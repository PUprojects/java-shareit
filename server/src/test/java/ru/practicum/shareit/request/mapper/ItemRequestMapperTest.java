package ru.practicum.shareit.request.mapper;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.RequestAnswerDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestMapperTest {
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

    private final ItemRequestMapper itemRequestMapper = new ItemRequestMapperImpl();
    private final User requester = new User(1, "First user", "first@mail.ru");
    private final ItemRequest itemRequest = new ItemRequest(1, "Need item",
            LocalDateTime.now().minusHours(1), requester);

    @Test
    void toItemRequestDtoShouldReturnNullWhenRequestIsNull() {
        assertNull(itemRequestMapper.toItemRequestDto(null), "Возвращаемое значение должно быть null");
    }

    @Test
    void toItemRequestWithAnswersDtoShouldReturnNullWhenInParametersNull() {
        assertNull(itemRequestMapper.toItemRequestWithAnswersDto(null, null),
                "Возвращаемое значение должно быть null");
    }

    @Test
    void toItemRequestDtoShouldMapRequestToDto() {
        ItemRequestDto mappedDto = itemRequestMapper.toItemRequestDto(itemRequest);

        assertEquals(itemRequest.getId(), mappedDto.id(), "id не совпадает");
        assertEquals(itemRequest.getDescription(), mappedDto.description(), "Описание не совпадает");
        assertEquals(itemRequest.getCreated().toString(), mappedDto.created().toString(), "Время создания не совпадает");
    }

    @Test
    void toItemRequestWithAnswersDtoShouldMapToDto() {
        List<RequestAnswerDto> items = List.of(
                new RequestAnswerDtoTestImpl(1, "First item", 1),
                new RequestAnswerDtoTestImpl(2, "SecondItem", 2)
        );

       ItemRequestWithAnswersDto mappedDto = itemRequestMapper.toItemRequestWithAnswersDto(itemRequest, items);

        assertEquals(itemRequest.getId(), mappedDto.id(), "id не совпадает");
        assertEquals(itemRequest.getDescription(), mappedDto.description(), "Описание не совпадает");
        assertEquals(itemRequest.getCreated().toString(), mappedDto.created().toString(), "Время создания не совпадает");
        assertEquals(items.size(), mappedDto.items().size(), "Размер списка ответов не совпадает");
        assertEquals(items.get(0), mappedDto.items().get(0), "Первый ответ не совпадает");
        assertEquals(items.get(1), mappedDto.items().get(1), "Второй ответ не совпадает");
    }

    @Test
    void toItemRequestWithAnswersDtoShouldMapToDtoWithNoAnswers() {

       ItemRequestWithAnswersDto mappedDto = itemRequestMapper.toItemRequestWithAnswersDto(itemRequest, null);

        assertEquals(itemRequest.getId(), mappedDto.id(), "id не совпадает");
        assertEquals(itemRequest.getDescription(), mappedDto.description(), "Описание не совпадает");
        assertEquals(itemRequest.getCreated().toString(), mappedDto.created().toString(), "Время создания не совпадает");
        assertNull(mappedDto.items(), "Список ответов должен быть null");
    }
}