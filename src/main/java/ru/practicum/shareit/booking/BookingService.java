package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {
    BookingDto create(NewBookingDto newBookingDto, long ownerId);

    BookingDto approve(long bookingId, boolean isAvailable, long userId);

    Booking findBookingById(long bookingId);

    BookingDto findById(long bookingId, long userId);

    List<BookingDto> getByState(String status, long userId);

    List<BookingDto> getByOwnerAndState(String status, long userId);
}
