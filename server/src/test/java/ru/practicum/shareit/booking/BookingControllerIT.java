package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.constants.AppConstants;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerIT {
    @MockitoBean
    BookingServiceImpl bookingService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    MockMvc mvc;

    private final List<BookingDto> bookings = List.of(
            new BookingDto(1, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
                    null, null, BookingStatus.WAITING),
            new BookingDto(2, LocalDateTime.now().plusHours(10), LocalDateTime.now().plusHours(20),
                    null, null, BookingStatus.APPROVED)
    );

    @Test
    @DisplayName("bookItem должна создавать бронирование")
    void shouldCreateNewBookingWhenInputDataCorrect() throws Exception {
        NewBookingDto newBookingDto = new NewBookingDto(5, bookings.getFirst().getStart(),
                bookings.getFirst().getEnd());
        when(bookingService.create(any(), anyLong()))
                .thenReturn(bookings.getFirst());

        RequestBuilder request = post("/bookings")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(newBookingDto));

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id", is(bookings.getFirst().getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookings.getFirst().getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end", is(bookings.getFirst().getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        verify(bookingService, times(1)).create(any(), anyLong());
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    @DisplayName("approve должна подтверждать бронирование")
    void shouldApproveBooking() throws Exception {
        when(bookingService.approve(2, true, 7))
                .thenReturn(bookings.get(1));

        RequestBuilder request = patch("/bookings/2?approved=true")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 7L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id", is(bookings.get(1).getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookings.get(1).getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end", is(bookings.get(1).getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.status", is(BookingStatus.APPROVED.toString())));

        verify(bookingService, times(1)).approve(2, true, 7);
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    @DisplayName("getBooking должна возвращать бронирование по id")
    void shouldGetBookingById() throws Exception {
        when(bookingService.findById(2L, 4L))
                .thenReturn(bookings.get(1));

        RequestBuilder request = get("/bookings/2")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 4L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id", is(bookings.get(1).getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookings.get(1).getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end", is(bookings.get(1).getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.status", is(BookingStatus.APPROVED.toString())));

        verify(bookingService, times(1)).findById(2L, 4L);
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    @DisplayName("getBookings должна возвращать бронирования указанного пользователя")
    void shouldGetBookingsByBookerId() throws Exception {
        when(bookingService.getByState(any(), anyLong()))
                .thenReturn(bookings);

        RequestBuilder request = get("/bookings")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 2L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id", is(bookings.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(bookings.get(1).getId()), Long.class));

        verify(bookingService, times(1)).getByState(any(), anyLong());
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    @DisplayName("getByOwnerAndState должна возвращать бронирования вещей указанного пользователя")
    void shouldGetBookingsByOwnerId() throws Exception {
        when(bookingService.getByOwnerAndState(any(), anyLong()))
                .thenReturn(bookings);

        RequestBuilder request = get("/bookings/owner")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 2L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id", is(bookings.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(bookings.get(1).getId()), Long.class));

        verify(bookingService, times(1)).getByOwnerAndState(any(), anyLong());
        verifyNoMoreInteractions(bookingService);
    }

}