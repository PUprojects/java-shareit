package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

@AllArgsConstructor
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemWithBookingAndCommentsDto {
    final Long id;
    final String name;
    final String description;
    final Boolean available;
    final Long requestId;
    final BookingDto lastBooking;
    final BookingDto nextBooking;
    final List<CommentDto> comments;
}
