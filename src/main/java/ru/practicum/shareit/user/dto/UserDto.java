package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {
    final Long id;
    @NotBlank(message = "Имя пользователя не может быть пустым")
    final String name;
    @Email(message = "Ошибка в формате email")
    @NotBlank(message = "email не может быть пустым")
    final String email;
}
