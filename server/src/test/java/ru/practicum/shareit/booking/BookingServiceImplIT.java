package ru.practicum.shareit.booking;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class BookingServiceImplIT {
    private final EntityManager em;
    private final BookingServiceImpl bookingService;
    private final BookingMapper bookingMapper;

    private final List<BookingDto> bookings = List.of(
            new BookingDto(1, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
                    null, null, BookingStatus.WAITING),
            new BookingDto(2, LocalDateTime.now().plusHours(10), LocalDateTime.now().plusHours(20),
                    null, null, BookingStatus.APPROVED)
    );

    private Booking getFromDB(long id) {
        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        query.setParameter("id", id);
        return query.getSingleResult();
    }

    @Test
    @DisplayName("create должна создаватьнового пользователя в базе данных")
    void createShouldCreateNewBookingInDatabase() {
        NewBookingDto newBookingDto = new NewBookingDto(1, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2));

        BookingDto createdDto = bookingService.create(newBookingDto, 1L);
        Booking bookingFromDB = getFromDB(createdDto.getId());
        BookingDto newBookingDtoFromDB = bookingMapper.toBookingDto(bookingFromDB);

        assertNotNull(newBookingDtoFromDB);
        assertEquals(createdDto, newBookingDtoFromDB);
        assertEquals(newBookingDtoFromDB.getStart(), newBookingDto.start());
        assertEquals(newBookingDtoFromDB.getEnd(), newBookingDto.end());
        assertEquals(newBookingDtoFromDB.getItem().getId(), newBookingDto.itemId());
    }

}