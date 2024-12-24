package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDto {
    final Long id;
    @NotBlank(message = "Название не может быть пустым")
    final String name;
    @NotBlank(message = "Описание не может быть пустым")
    final String description;
    @NotNull(message = "Должна быть указана доступность вещи")
    final Boolean available;
    final Long requestId;
}
