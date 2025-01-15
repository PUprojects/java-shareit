package ru.practicum.shareit.item.dto;

import java.time.LocalDateTime;

public record CommentDto(long id, String text, long itemId, String authorName, LocalDateTime created) {
}
