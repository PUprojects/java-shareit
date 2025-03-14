package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.constants.AppConstants;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto create(@RequestBody NewBookingDto newBookingDto,
                             @RequestHeader(AppConstants.UserIdHeader) long userId) {
        log.info("От пользователя {} получен запрос POST /bookings: {}", userId, newBookingDto);
        return bookingService.create(newBookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@PathVariable("bookingId") long bookingId,
                              @RequestParam(value = "approved") boolean isApproved,
                              @RequestHeader(AppConstants.UserIdHeader) long userId) {
        log.info("От пользователя {} получен запрос PATCH /bookings/{}?approved={}", userId, bookingId, isApproved);
        return bookingService.approve(bookingId, isApproved, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto get(@PathVariable("bookingId") long bookingId,
                          @RequestHeader(AppConstants.UserIdHeader) long userId) {
        log.info("От пользователя {} получен запрос GET /bookings/{}", userId, bookingId);
        return bookingService.findById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getByState(@RequestParam(value = "state", required = false, defaultValue = "ALL") String state,
                                       @RequestHeader(AppConstants.UserIdHeader) long userId) {
        log.info("От пользователя {} получен запрос GET /bookings?state={}", userId, state);
        return bookingService.getByState(state, userId);
    }

    @GetMapping("/owner")
    public List<BookingDto> getByOwnerAndState(@RequestParam(value = "state", required = false, defaultValue = "ALL") String state,
                                               @RequestHeader(AppConstants.UserIdHeader) long userId) {
        log.info("От пользователя {} получен запрос GET /bookings/owner?state={}", userId, state);
        return bookingService.getByOwnerAndState(state, userId);
    }
}
