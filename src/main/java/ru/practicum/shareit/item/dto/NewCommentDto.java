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
public class NewCommentDto {
    long id;
    @NotBlank(message = "Содержание комментария не может быть пустым")
    String text;
    @NotNull(message = "Должен быть указан id вещи")
    long itemId;
    @NotNull(message = "Должен быть указан id автора")
    long authorId;
}
