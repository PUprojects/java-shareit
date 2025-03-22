package ru.practicum.shareit.booking;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.ErrorResponse;
import ru.practicum.shareit.constants.AppConstants;


@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;

	@PostMapping
	public ResponseEntity<Object> bookItem(@RequestHeader(AppConstants.UserIdHeader) long userId,
										   @RequestBody @Valid BookItemRequestDto requestDto) {
		log.info("Creating booking {}, userId={}", requestDto, userId);
		return bookingClient.bookItem(userId, requestDto);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> approve(@PathVariable("bookingId") long bookingId,
										  @RequestParam(value = "approved") boolean isApproved,
										  @RequestHeader(AppConstants.UserIdHeader) long userId) {
		log.info("От пользователя {} получен запрос PATCH /bookings/{}?approved={}", userId, bookingId, isApproved);
		return bookingClient.approve(userId, bookingId, isApproved);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBooking(@RequestHeader(AppConstants.UserIdHeader) long userId,
											 @PathVariable Long bookingId) {
		log.info("Get booking {}, userId={}", bookingId, userId);
		return bookingClient.getBooking(userId, bookingId);
	}

	@GetMapping
	public ResponseEntity<Object> getBookings(@RequestHeader(AppConstants.UserIdHeader) long userId,
			@RequestParam(name = "state", defaultValue = "all") String stateParam,
											  @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
											  @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.getBookings(userId, state, from, size);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getByOwnerAndState(@RequestHeader(AppConstants.UserIdHeader) long userId,
													 @RequestParam(value = "state", required = false, defaultValue = "ALL") String stateParam) {
		log.info("От пользователя {} получен запрос GET /bookings/owner?state={}", userId, stateParam);
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));

		return bookingClient.getByOwnerAndState(userId, state);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleIllegalArgumentException(final IllegalArgumentException e) {
		log.debug("Error ", e);
		return new ErrorResponse(e.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleConstraintViolationException(final ConstraintViolationException e) {
		log.debug("Error ", e);
		return new ErrorResponse(e.getMessage());
	}
}
