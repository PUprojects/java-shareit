package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {
    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    private final List<BookingDto> bookings = List.of(
            new BookingDto(1, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
                    null, null, BookingStatus.WAITING),
            new BookingDto(2, LocalDateTime.now().plusHours(10), LocalDateTime.now().plusHours(20),
                    null, null, BookingStatus.APPROVED)
    );

    @Test
    void createShouldReturnBookingDtoWhenInvoked() {
        NewBookingDto newBookingDto = new NewBookingDto(5, bookings.getFirst().getStart(),
                bookings.getFirst().getEnd());
        when(bookingService.create(newBookingDto, 1L)).thenReturn(bookings.getFirst());

        BookingDto resultDto = bookingController.create(newBookingDto, 1L);

        assertEquals(bookings.getFirst(), resultDto, "Возвращённые данные не соответсвуют ожидаемым");
        verify(bookingService, times(1)).create(newBookingDto, 1L);
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void approveShouldReturnBookingDtoWhenInvoked() {
        when(bookingService.approve(2, true, 7))
                .thenReturn(bookings.get(1));

        BookingDto resultDto = bookingController.approve(2, true, 7);

        assertEquals(bookings.get(1), resultDto, "Возвращённые данные не соответсвуют ожидаемым");
        verify(bookingService, times(1)).approve(2, true, 7);
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void getShouldReturnBookingDtoWhenInvoked() {
        when(bookingService.findById(2L, 4L))
                .thenReturn(bookings.get(1));

        BookingDto resultDto = bookingController.get(2L, 4L);

        assertEquals(bookings.get(1), resultDto, "Возвращённые данные не соответсвуют ожидаемым");
        verify(bookingService, times(1)).findById(2L, 4L);
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void getByStateShouldReturnListOfBookingDtoWhenInvoked() {
        when(bookingService.getByState("ALL", 2L))
                .thenReturn(bookings);

        List<BookingDto> resultList = bookingController.getByState("ALL", 2L);

        assertEquals(bookings, resultList, "Возвращённые данные не соответсвуют ожидаемым");
        verify(bookingService, times(1)).getByState("ALL", 2L);
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void getByOwnerAndStateShouldReturnListOfBookingDtoWhenInvoked() {
        when(bookingService.getByOwnerAndState("ALL", 2L))
                .thenReturn(bookings);

        List<BookingDto> resultList = bookingController.getByOwnerAndState("ALL", 2L);

        assertEquals(bookings, resultList, "Возвращённые данные не соответсвуют ожидаемым");
        verify(bookingService, times(1)).getByOwnerAndState("ALL", 2L);
        verifyNoMoreInteractions(bookingService);
    }
}