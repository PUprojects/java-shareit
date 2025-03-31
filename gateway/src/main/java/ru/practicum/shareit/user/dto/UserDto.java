package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserDto(Long id, @NotBlank(message = "Имя пользователя не может быть пустым") String name,
                      @Email(message = "Ошибка в формате email") @NotBlank(message = "email не может быть пустым") String email) {
}
