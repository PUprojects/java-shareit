package ru.practicum.shareit.request.dto;

import java.time.LocalDateTime;

public record ItemRequestDto(long id, String description, LocalDateTime created) {
}
