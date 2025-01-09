package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record NewBookingDto(@NotNull(message = "Не передан идентификатор вещи") long itemId,
                            @FutureOrPresent(message = "Дата начала аренды должна быть в будущем или настоящем") @NotNull(message = "Не передано время начала аренды") LocalDateTime start,
                            @Future(message = "Дата завершения аренды должна быть в будущем") @NotNull(message = "Не передано время завершения аренды") LocalDateTime end) {
}
