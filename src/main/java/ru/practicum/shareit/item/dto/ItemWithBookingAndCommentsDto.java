package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public record ItemWithBookingAndCommentsDto(Long id, String name, String description, Boolean available, Long requestId,
                                            BookingDto lastBooking, BookingDto nextBooking, List<CommentDto> comments) {
}
