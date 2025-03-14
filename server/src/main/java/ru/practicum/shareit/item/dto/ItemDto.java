package ru.practicum.shareit.item.dto;

public record ItemDto(Long id, String name, String description, Boolean available, Long requestId) {
}
