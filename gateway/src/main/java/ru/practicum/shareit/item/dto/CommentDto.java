package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentDto(
        @NotBlank(message = "Содержание комментария не может быть пустым") @Size(max = 255, message = "Комментарий не может быть длинее 255 символов") String text) {
}
