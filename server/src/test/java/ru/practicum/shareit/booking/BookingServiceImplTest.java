package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserService userService;
    @Mock
    private ItemService itemService;
    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Captor
    private ArgumentCaptor<Booking> bookingArgumentCaptor;

    private final List<User> users = List.of(
            new User(1, "First user", "first@mail.ru"),
            new User(2, "Second user", "second@mail.ru"),
            new User(3, "Third user", "third@mail.ru")
    );

    private final List<Item> items = List.of(
            new Item(1, "First item", "First item desc",
                    true, users.get(1), null),
            new Item(2, "Second item", "Not available item desc",
                    false, users.getFirst(), null)
    );

    private final List<Booking> bookings = List.of(
            new Booking(1, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
                    items.getFirst(), users.getFirst(), BookingStatus.WAITING),
            new Booking(2, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(2),
                    items.getFirst(), users.getFirst(), BookingStatus.APPROVED),
            new Booking(3, LocalDateTime.now().minusHours(4), LocalDateTime.now().minusHours(2),
                    items.getFirst(), users.getFirst(), BookingStatus.REJECTED)

    );

    private final List<BookingDto> bookingsDtos = List.of(
            new BookingDto(1, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
                    items.getFirst(), users.getFirst(), BookingStatus.WAITING),
            new BookingDto(2, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(2),
                    items.getFirst(), users.getFirst(), BookingStatus.APPROVED),
            new BookingDto(3, LocalDateTime.now().minusHours(4), LocalDateTime.now().minusHours(2),
                    items.getFirst(), users.getFirst(), BookingStatus.REJECTED)
    );

    @Test
    void createShouldCreateBookingWhenInDataValid() {
        NewBookingDto newBookingDto = new NewBookingDto(1, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2));
        when(userService.getUserById(1)).thenReturn(users.getFirst());
        when(itemService.getItemById(1)).thenReturn(items.getFirst());

        bookingService.create(newBookingDto, 1);

        verify(bookingRepository).save(bookingArgumentCaptor.capture());
        Booking createdBooking = bookingArgumentCaptor.getValue();
        assertEquals(newBookingDto.start(), createdBooking.getStart(),
                "Время начала бронирования не совпадает");
        assertEquals(newBookingDto.end(), createdBooking.getEnd(),
                "Время завершения бронирования не совпадает");
        assertEquals(newBookingDto.itemId(), createdBooking.getItem().getId(),
                "id предмета не совпадает");
        assertEquals(1L, createdBooking.getBooker().getId(),
                "id бронирующего пользователя не совпадает");
        assertEquals(BookingStatus.WAITING, createdBooking.getStatus(),
                "Статус бронирования не WAITING");
        verify(userService, times(1)).getUserById(1);
        verify(itemService, times(1)).getItemById(1);
        verify(bookingMapper, times(1)).toBookingDto(any());
        verifyNoMoreInteractions(bookingRepository, userService, itemService, bookingMapper);
    }

    @Test
    void createShouldThrowExceptionWhenBookingDateIncorrect() {
        NewBookingDto newBookingDto = new NewBookingDto(1, LocalDateTime.now().plusHours(5),
                LocalDateTime.now().plusHours(2));
        when(userService.getUserById(1)).thenReturn(users.getFirst());
        when(itemService.getItemById(1)).thenReturn(items.getFirst());

        InvalidBookingDateException exception = assertThrows(InvalidBookingDateException.class,
                () -> bookingService.create(newBookingDto, 1));
        assertEquals("Дата завершения бронирования должна быть позже даты начала",
                exception.getMessage());
        verify(userService, times(1)).getUserById(1);
        verify(itemService, times(1)).getItemById(1);
        verifyNoMoreInteractions(bookingRepository, userService, itemService, bookingMapper);
    }

    @Test
    void createShouldThrowExceptionWhenItemNotAvailable() {
        NewBookingDto newBookingDto = new NewBookingDto(2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2));
        when(userService.getUserById(2)).thenReturn(users.get(1));
        when(itemService.getItemById(2)).thenReturn(items.get(1));

        ItemUnavailableException exception = assertThrows(ItemUnavailableException.class,
                () -> bookingService.create(newBookingDto, 2));
        assertEquals("Вещь " + items.get(1).getId() + " уже забронирована",
                exception.getMessage());
        verify(userService, times(1)).getUserById(2);
        verify(itemService, times(1)).getItemById(2);
        verifyNoMoreInteractions(bookingRepository, userService, itemService, bookingMapper);
    }

    @Test
    void approveShouldSetApproveBookingForItemOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookings.getFirst()));

        bookingService.approve(1, true, 2);

        verify(bookingRepository).save(bookingArgumentCaptor.capture());
        Booking updatingBooking = bookingArgumentCaptor.getValue();
        assertEquals(BookingStatus.APPROVED, updatingBooking.getStatus(),
                "Статус бронирования установлен не верно");
        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingMapper, times(1)).toBookingDto(any());
        verifyNoMoreInteractions(bookingRepository, userService, itemService, bookingMapper);
    }

    @Test
    void approveShouldRejectBookingForItemOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookings.getFirst()));

        bookingService.approve(1, false, 2);

        verify(bookingRepository).save(bookingArgumentCaptor.capture());
        Booking updatingBooking = bookingArgumentCaptor.getValue();
        assertEquals(BookingStatus.REJECTED, updatingBooking.getStatus(),
                "Статус бронирования установлен не верно");
        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingMapper, times(1)).toBookingDto(any());
        verifyNoMoreInteractions(bookingRepository, userService, itemService, bookingMapper);
    }

    @Test
    void approveShouldThrowExceptionWhenUserIsNotItemOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookings.getFirst()));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> bookingService.approve(1, false, 1));

        assertEquals("Пользователь 1 не является хозяином вещи",
                exception.getMessage());
        verify(bookingRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(bookingRepository, userService, itemService, bookingMapper);
    }

    @Test
    void findBookingByIdShouldThrowExceptionWhenBookingNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.findBookingById(1L));
        assertEquals("Бронирование 1 не найдено", exception.getMessage());
        verify(bookingRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(bookingRepository, userService, itemService, bookingMapper);
    }

    @Test
    void findByIdShouldReturnBookingDtoWhenUserIsOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookings.getFirst()));
        when(bookingMapper.toBookingDto(bookings.getFirst())).thenReturn(bookingsDtos.getFirst());

        BookingDto resultDto = bookingService.findById(1L, 2L);

        assertEquals(bookingsDtos.getFirst(), resultDto, "Возвращённые данные не соответствуют ожидаемым");
        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingMapper, times(1)).toBookingDto(bookings.getFirst());
        verifyNoMoreInteractions(bookingRepository, userService, itemService, bookingMapper);
    }

    @Test
    void findByIdShouldReturnBookingDtoWhenUserIsBooker() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookings.getFirst()));
        when(bookingMapper.toBookingDto(bookings.getFirst())).thenReturn(bookingsDtos.getFirst());

        BookingDto resultDto = bookingService.findById(1L, 1L);

        assertEquals(bookingsDtos.getFirst(), resultDto, "Возвращённые данные не соответствуют ожидаемым");
        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingMapper, times(1)).toBookingDto(bookings.getFirst());
        verifyNoMoreInteractions(bookingRepository, userService, itemService, bookingMapper);
    }

    @Test
    void findByIdShouldThrowExceptionWhenUserIsNotBookerAndIsNotOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookings.getFirst()));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> bookingService.findById(1L, 3L));

        assertEquals("Пользователю 3 доступ к бронированию запрещён", exception.getMessage());
        verify(bookingRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(bookingRepository, userService, itemService, bookingMapper);
    }

    @Test
    void getByStateShouldReturnListOfBookingDtoWhenInvoked() {
        when(bookingRepository.findAllByBooker_Id(anyLong(), any())).thenReturn(bookings);
        when(bookingMapper.toBookingDto(any())).thenReturn(bookingsDtos.getFirst());

        List<BookingDto> resultBookingsDtos = bookingService.getByState("ALL", 1L);

        assertEquals(bookings.size(), resultBookingsDtos.size(), "Размер возвращённого списка неверен");
        assertEquals(bookingsDtos.getFirst(), resultBookingsDtos.getFirst(), "Возвращённые данные не соответствуют ожидаемым");
        verify(bookingRepository, times(1)).findAllByBooker_Id(anyLong(), any());
        verify(bookingMapper, times(3)).toBookingDto(any());
        verifyNoMoreInteractions(bookingRepository, userService, itemService, bookingMapper);
    }

    @Test
    void getByOwnerAndStateShouldReturnListOfBookingDtoWhenInvoked() {
        when(bookingRepository.findAllByOwner(anyLong(), any())).thenReturn(bookings);
        when(bookingMapper.toBookingDto(any())).thenReturn(bookingsDtos.getFirst());

        List<BookingDto> resultBookingsDtos = bookingService.getByOwnerAndState("ALL", 1L);

        assertEquals(bookings.size(), resultBookingsDtos.size(), "Размер возвращённого списка неверен");
        assertEquals(bookingsDtos.getFirst(), resultBookingsDtos.getFirst(), "Возвращённые данные не соответствуют ожидаемым");
        verify(bookingRepository, times(1)).findAllByOwner(anyLong(), any());
        verify(bookingMapper, times(3)).toBookingDto(any());
        verifyNoMoreInteractions(bookingRepository, userService, itemService, bookingMapper);
    }

    @Test
    void getByOwnerAndStateShouldThrowExceptionWhenNoBookingsFound() {
        when(bookingRepository.findAllByOwner(anyLong(), any())).thenReturn(List.of());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getByOwnerAndState("ALL", 1L));

        assertEquals("Бронирований не найдено", exception.getMessage());
        verify(bookingRepository, times(1)).findAllByOwner(anyLong(), any());
        verifyNoMoreInteractions(bookingRepository, userService, itemService, bookingMapper);
    }

    private Method getFilterAndMapMethod()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = BookingServiceImpl.class.getDeclaredMethod("filterAndMapBookings",
                List.class, String.class);
        method.setAccessible(true);
        return method;
    }

    @Test
    void filterAndMapBookingsShouldReturnEmptyListWhenStatusInvalid()
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Method method = getFilterAndMapMethod();

        @SuppressWarnings("unchecked") List<BookingDto> resultBookingsDtos =
                (List<BookingDto>) method.invoke(bookingService, bookings, "BEST");

        assertTrue(resultBookingsDtos.isEmpty(), "Возвращённый список должен быть пустым");
        verifyNoInteractions(bookingRepository, userService, itemService, bookingMapper);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "CURRENT, 1",
            "FUTURE, 0",
            "PAST, 2",
            "WAITING, 1",
            "REJECTED, 2"
    }, ignoreLeadingAndTrailingWhitespace = true)
    void filterAndMapBookingsShouldFilterPastBooking(String status, int dtoIndex)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Method method = getFilterAndMapMethod();

        when(bookingMapper.toBookingDto(any())).thenReturn(bookingsDtos.get(dtoIndex));

        @SuppressWarnings("unchecked") List<BookingDto> resultBookingsDtos =
                (List<BookingDto>) method.invoke(bookingService, bookings, status);

        assertEquals(1, resultBookingsDtos.size(), "Размер возвращённого списка неверен");
        assertEquals(bookingsDtos.get(dtoIndex), resultBookingsDtos.getFirst(), "Возвращённые данные не соответствуют ожидаемым");
        verify(bookingMapper, times(1)).toBookingDto(any());
        verifyNoMoreInteractions(bookingRepository, userService, itemService, bookingMapper);
    }
}