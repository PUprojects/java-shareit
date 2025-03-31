package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {
    private final BookingMapper bookingMapper = new BookingMapperImpl();

    @Test
    void toBookingDtoShouldMapBookingToBookingDto() {
        User user = new User(1, "First user", "first@mail.ru");
        Item item = new Item(1, "First item", "First item desc",
                true, user, null);
        Booking booking =  new Booking(1, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
                item, user, BookingStatus.WAITING);

        BookingDto bookingDto = bookingMapper.toBookingDto(booking);

        assertEquals(booking.getId(), bookingDto.getId(), "id не совпадает");
        assertEquals(booking.getStart(), bookingDto.getStart(), "Время начала не совпадает");
        assertEquals(booking.getEnd(), bookingDto.getEnd(), "Время завершения не совпадает");
        assertEquals(booking.getStatus(), bookingDto.getStatus(), "Статус не совпадает");
        assertEquals(booking.getItem().getId(), bookingDto.getItem().getId(), "id предмета не совпадает");
        assertEquals(booking.getBooker().getId(), bookingDto.getItem().getId(), "id арендатора не совпадает");
    }

    @Test
    void  toBookingDtoShouldReturnNullWhenBookingIsNull() {
        assertNull(bookingMapper.toBookingDto(null), "Возвращаемое значение должно быть null");
    }
}