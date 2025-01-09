package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.InvalidBookingDateException;
import ru.practicum.shareit.exception.ItemUnavailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingMapper bookingMapper;

    @Override
    public BookingDto create(NewBookingDto newBookingDto, long bookerId) {
        User booker = userService.getUserById(bookerId);
        Item item = itemService.getItemById(newBookingDto.itemId());

        if (!newBookingDto.end().isAfter(newBookingDto.start())) {
            throw new InvalidBookingDateException("Дата завершения бронирования должна быть позже даты начала");
        }

        if (!item.isAvailable()) {
            throw new ItemUnavailableException("Вещь " + item.getId() + " уже забронирована");
        }

        Booking booking = new Booking();
        booking.setStart(newBookingDto.start());
        booking.setEnd(newBookingDto.end());
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        return bookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto approve(long bookingId, boolean isAvailable, long userId) {
        Booking booking = findBookingById(bookingId);
        if (booking.getItem().getOwner().getId() != userId) {
            throw new AccessDeniedException("Пользователь " + userId + " не является хозяином вещи");
        }

        booking.setStatus(isAvailable ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        bookingRepository.save(booking);

        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public Booking findBookingById(long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование " + bookingId + " не найдено"));
    }

    @Override
    public BookingDto findById(long bookingId, long userId) {
        Booking booking = findBookingById(bookingId);

        if ((booking.getBooker().getId() != userId) && (booking.getItem().getOwner().getId() != userId)) {
            throw new AccessDeniedException("Пользователю " + userId + " доступ к бронированию запрещён");
        }

        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getByState(String status, long userId) {
        List<Booking> bookings;
        final Sort sort = Sort.by("start");
        bookings = bookingRepository.findAllByBooker_Id(userId, sort);

        return filterAndMapBookings(bookings, status);
    }

    @Override
    public List<BookingDto> getByOwnerAndState(String status, long userId) {
        List<Booking> bookings;
        final Sort sort = Sort.by("start");

        bookings = bookingRepository.findAllByOwner(userId, sort);

        if (bookings.isEmpty()) {
            throw new NotFoundException("Бронирований не найдено");
        }

        return filterAndMapBookings(bookings, status);
    }

    private List<BookingDto> filterAndMapBookings(List<Booking> bookings, String status) {
        return switch (status.toUpperCase()) {
            case "ALL" -> bookings
                    .stream()
                    .map(bookingMapper::toBookingDto)
                    .toList();
            case "CURRENT" -> bookings
                    .stream()
                    .filter(booking -> (booking.getStart().isBefore(LocalDateTime.now()) &&
                            (!booking.getEnd().isBefore(LocalDateTime.now()))))
                    .map(bookingMapper::toBookingDto)
                    .toList();
            case "PAST" -> bookings
                    .stream()
                    .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                    .map(bookingMapper::toBookingDto)
                    .toList();
            case "FUTURE" -> bookings
                    .stream()
                    .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                    .map(bookingMapper::toBookingDto)
                    .toList();
            case "WAITING" -> bookings
                    .stream()
                    .filter(booking -> booking.getStatus() == BookingStatus.WAITING)
                    .map(bookingMapper::toBookingDto)
                    .toList();
            case "REJECTED" -> bookings
                    .stream()
                    .filter(booking -> booking.getStatus() == BookingStatus.REJECTED)
                    .map(bookingMapper::toBookingDto)
                    .toList();
            default -> List.of();
        };
    }
}
