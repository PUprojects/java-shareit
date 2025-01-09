package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemDto(Long id, @NotBlank(message = "Название не может быть пустым") String name,
                      @NotBlank(message = "Описание не может быть пустым") String description,
                      @NotNull(message = "Должна быть указана доступность вещи") Boolean available, Long requestId) {
}
