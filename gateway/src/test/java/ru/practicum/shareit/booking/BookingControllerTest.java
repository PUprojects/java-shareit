package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.constants.AppConstants;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    static class BadBookingsArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of(new BookItemRequestDto(1, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1))),
                    Arguments.of(new BookItemRequestDto(1, LocalDateTime.now().plusDays(1), LocalDateTime.now().minusDays(1))));
        }
    }

    @MockitoBean
    BookingClient bookingClient;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    MockMvc mvc;

    private final List<BookItemRequestDto> bookings = List.of(
            new BookItemRequestDto(1, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2)),
            new BookItemRequestDto(2, LocalDateTime.now().plusHours(10), LocalDateTime.now().plusHours(20)));

    @Test
    @DisplayName("bookItem должна создавать бронирование, если входные данные корректны")
    void shouldCreateNewBookingWhenInputDataCorrect() throws Exception {
        when(bookingClient.bookItem(anyLong(), any(BookItemRequestDto.class)))
                .thenReturn(ResponseEntity.created(URI.create("/bookings/1")).body(bookings.getFirst()));

        RequestBuilder request = post("/bookings")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(bookings.getFirst()));

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.itemId", is(bookings.getFirst().getItemId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookings.getFirst().getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end", is(bookings.getFirst().getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        verify(bookingClient, times(1)).bookItem(anyLong(), any(BookItemRequestDto.class));
        verifyNoMoreInteractions(bookingClient);
    }

    @ParameterizedTest(name = "{index} - bookItem не должна создавать бронирование, если входные данные не корректны")
    @ArgumentsSource(BadBookingsArgumentsProvider.class)
    void shouldNotCreateNewBookingWhenInputDataInvalid(BookItemRequestDto badRequest) throws Exception {
        RequestBuilder request = post("/bookings")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(badRequest));

        mvc.perform(request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    @DisplayName("approve должна подтверждать бронирование")
    void shouldApproveBooking() throws Exception {
        when(bookingClient.approve(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(ResponseEntity.ok().body(bookings.getFirst()));

        RequestBuilder request = patch("/bookings/5?approved=true")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 2L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty());

        verify(bookingClient, times(1)).approve(anyLong(), anyLong(), anyBoolean());
        verifyNoMoreInteractions(bookingClient);
    }

    @Test
    @DisplayName("approve не должна подтверждать бронирование, если не указан id пользователя")
    void shouldNotApproveBookingWhenNoUserId() throws Exception {
        RequestBuilder request = patch("/bookings/5?approved=true")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    @DisplayName("getBooking должна возвращать бронирование по id")
    void shouldGetBookingById() throws Exception {
        when(bookingClient.getBooking(2L, 4L))
                .thenReturn(ResponseEntity.ok().body(bookings.getFirst()));

        RequestBuilder request = get("/bookings/4")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 2L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.itemId", is(bookings.getFirst().getItemId()), Long.class));

        verify(bookingClient, times(1)).getBooking(2L, 4L);
        verifyNoMoreInteractions(bookingClient);
    }

    @Test
    @DisplayName("getBooking не должна возвращать данные, если не указан id пользователя")
    void shouldNotGetBookingByIdWhenNoUserId() throws Exception {
        RequestBuilder request = get("/bookings/5")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    @DisplayName("getBookings должна возвращать бронирования указанного пользователя")
    void shouldGetBookingsByBookerId() throws Exception {
        when(bookingClient.getBookings(2, BookingState.ALL, 5, 15))
                .thenReturn(ResponseEntity.ok().body(bookings));

        RequestBuilder request = get("/bookings?from=5&size=15")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 2L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].itemId", is(bookings.get(0).getItemId()), Long.class))
                .andExpect(jsonPath("$[1].itemId", is(bookings.get(1).getItemId()), Long.class));

        verify(bookingClient, times(1)).getBookings(2, BookingState.ALL, 5, 15);
        verifyNoMoreInteractions(bookingClient);
    }

    @ParameterizedTest(name = "{index} - getBookings не должна возвращать данные при некорректном запросе")
    @CsvSource(value = {
            "NONE, 7, 8",
            "ALL, -4, 10",
            "FUTURE, 1, -4"
    })
    void shouldNotGetBookingsByBookerIdWhenWrongRequest(String state, int from, int size) throws Exception {
        String uriTemplate = "/bookings?state=" + state + "&from=" + from + "&size=" + size;
        RequestBuilder request = get(uriTemplate)
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 2L);

        mvc.perform(request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    @DisplayName("getByOwnerAndState должна возвращать бронирования вещей указанного пользователя")
    void shouldGetBookingsByOwnerId() throws Exception {
        when(bookingClient.getByOwnerAndState(2, BookingState.ALL))
                .thenReturn(ResponseEntity.ok().body(bookings));

        RequestBuilder request = get("/bookings/owner")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 2L);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].itemId", is(bookings.get(0).getItemId()), Long.class))
                .andExpect(jsonPath("$[1].itemId", is(bookings.get(1).getItemId()), Long.class));

        verify(bookingClient, times(1)).getByOwnerAndState(2, BookingState.ALL);
        verifyNoMoreInteractions(bookingClient);
    }

    @Test
    @DisplayName("getByOwnerAndState не должна возвращать данные при некорректном статусе в запросе")
    void shouldNotGetGetBookingsByOwnerIdWhenStatusInvalid() throws Exception {
        RequestBuilder request = get("/bookings/owner?state=NONE")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header(AppConstants.UserIdHeader, 2L);

        mvc.perform(request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }
}