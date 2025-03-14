package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

public record NewBookingDto(long itemId,
                            LocalDateTime start,
                             LocalDateTime end) {
}
