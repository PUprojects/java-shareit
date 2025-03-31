package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.dto.RequestAnswerDto;

import java.time.LocalDateTime;
import java.util.List;

public record ItemRequestWithAnswersDto(long id, String description, LocalDateTime created,
                                        List<RequestAnswerDto> items) {
}
