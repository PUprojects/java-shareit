package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ItemRequestDto(@NotBlank(message = "Описание запроса не может быть пустым")
                             @Size(max = 255, message = "Описание запроса не может быть длинее 255 символов") String description) {
}
