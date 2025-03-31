package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.InvalidAddCommentRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingAndCommentsDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserService userService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Captor
    ArgumentCaptor<Item> itemArgumentCaptor;
    @Captor
    ArgumentCaptor<Comment> commentArgumentCaptor;

    private final List<User> users = List.of(
            new User(1, "First user", "first@mail.ru"),
            new User(2, "Second user", "second@mail.ru"),
            new User(3, "Third user", "third@mail.ru")
    );

    private final List<ItemRequest> requests = List.of(
            new ItemRequest(1, "Need third item", LocalDateTime.now().minusHours(1),
                    users.get(2))
    );

    private final List<Item> items = List.of(
            new Item(1, "First item", "First item desc",
                    true, users.get(1), null),
            new Item(2, "Second item", "Second item desc",
                    false, users.getFirst(), null),
            new Item(2, "Third item", "Third item desc",
                    false, users.getFirst(), null)
    );

    private final List<ItemDto> itemsDto = List.of(
            new ItemDto(1L, "First item", "First item desc", true, null),
            new ItemDto(2L, "Second item", "Second item desc", false, null),
            new ItemDto(3L, "Third item", "Third item desc", true, 1L)
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
    void getAllByUserShouldReturnItemDtoListForOwner() {
        when(itemRepository.findByOwner(2)).thenReturn(List.of(items.getFirst()));
        when(itemMapper.toItemDto(any())).thenReturn(itemsDto.getFirst());

        List<ItemDto> resultList = itemService.getAllByUser(2);

        assertEquals(1, resultList.size(), "Размер возвращённого списка должен быть равен 1");
        assertEquals(itemsDto.getFirst(), resultList.getFirst(), "Возвращённые данные неравны ожидаемым");
        verify(itemRepository, times(1)).findByOwner(2);
        verify(itemMapper, times(1)).toItemDto(any());
        verifyNoMoreInteractions(itemMapper, itemRepository, bookingRepository, bookingMapper, commentMapper, commentRepository);
    }

    @Test
    void getByIdWithBookingsAndCommentsShouldReturnDtoWhenInvoked() {
        CommentDto commentDto = new CommentDto(1, "Comment", 1, "First User", LocalDateTime.now());
        when(itemMapper.toItemWithBookingAndCommentsDto(any(Item.class), any(BookingDto.class),
                any(BookingDto.class), anyList())).thenAnswer(arguments -> {
                    Item item = arguments.getArgument(0);
                    BookingDto last = arguments.getArgument(1);
                    BookingDto next = arguments.getArgument(2);
                    List<CommentDto> comments = arguments.getArgument(3);
                    Long requestId = item.getRequest() == null ? null : item.getRequest().getId();

                    return new ItemWithBookingAndCommentsDto(item.getId(), item.getName(), item.getDescription(),
                            item.isAvailable(), requestId, last, next, comments);
        });
        when(itemRepository.findById(1)).thenReturn(Optional.of(items.getFirst()));
        when(bookingRepository.findByItem_IdAndStartIsBeforeOrderByStartDesc(anyLong(), any()))
                .thenReturn(bookings.get(1));
        when(bookingMapper.toBookingDto(bookings.get(1))).thenReturn(bookingsDtos.get(1));
        when(bookingRepository.findByItem_IdAndEndIsAfterOrderByEnd(anyLong(), any())).thenReturn(bookings.get(0));
        when(bookingMapper.toBookingDto(bookings.get(0))).thenReturn(bookingsDtos.get(0));
        when(commentRepository.findAllByItem_Id(anyLong())).thenReturn(List.of(new Comment()));
        when(commentMapper.toCommentDto(any())).thenReturn(commentDto);

        ItemWithBookingAndCommentsDto resultDto = itemService.getByIdWithBookingsAndComments(1);

        assertEquals(items.getFirst().getId(), resultDto.id(), "id DTO должен быть " + items.getFirst().getId());
        assertEquals(bookingsDtos.get(1), resultDto.lastBooking(), "Не совпадают данные прошлого бронирования");
        assertEquals(bookingsDtos.get(0), resultDto.nextBooking(), "Не совпадают данные следующего бронирования");
        assertEquals(commentDto, resultDto.comments().getFirst(), "Не совпадают данные комментариев");

        verify(itemMapper, times(1)).toItemWithBookingAndCommentsDto(any(), any(), any(), anyList());
        verify(itemRepository, times(1)).findById(1);
        verify(bookingRepository, times(1)).findByItem_IdAndEndIsAfterOrderByEnd(anyLong(), any());
        verify(bookingRepository, times(1)).findByItem_IdAndStartIsBeforeOrderByStartDesc(anyLong(), any());
        verify(bookingMapper, times(2)).toBookingDto(any());
        verify(commentRepository, times(1)).findAllByItem_Id(1);
        verify(commentMapper, times(1)).toCommentDto(any());
        verifyNoMoreInteractions(itemMapper, itemRepository, bookingRepository, bookingMapper, commentMapper, commentRepository);
    }

    @Test
    void getByIdWithBookingsAndCommentsShouldReturnDtoWithNullBookingsWhenItNotFound() {
        CommentDto commentDto = new CommentDto(1, "Comment", 1, "First User", LocalDateTime.now());
        when(itemMapper.toItemWithBookingAndCommentsDto(any(Item.class), any(),
                any(), anyList())).thenAnswer(arguments -> {
                    Item item = arguments.getArgument(0);
                    BookingDto last = arguments.getArgument(1);
                    BookingDto next = arguments.getArgument(2);
                    List<CommentDto> comments = arguments.getArgument(3);
                    Long requestId = item.getRequest() == null ? null : item.getRequest().getId();

                    return new ItemWithBookingAndCommentsDto(item.getId(), item.getName(), item.getDescription(),
                            item.isAvailable(), requestId, last, next, comments);
        });
        when(itemRepository.findById(1)).thenReturn(Optional.of(items.getFirst()));
        when(bookingRepository.findByItem_IdAndStartIsBeforeOrderByStartDesc(anyLong(), any()))
                .thenReturn(null);
        when(bookingRepository.findByItem_IdAndEndIsAfterOrderByEnd(anyLong(), any())).thenReturn(null);
        when(commentRepository.findAllByItem_Id(anyLong())).thenReturn(List.of(new Comment()));
        when(commentMapper.toCommentDto(any())).thenReturn(commentDto);

        ItemWithBookingAndCommentsDto resultDto = itemService.getByIdWithBookingsAndComments(1);

        assertEquals(items.getFirst().getId(), resultDto.id(), "id DTO должен быть " + items.getFirst().getId());
        assertNull(resultDto.lastBooking(), "DTO прошлого бронирования должно быть null");
        assertNull(resultDto.nextBooking(), "DTO будущего бронирования должно быть null");
        assertEquals(commentDto, resultDto.comments().getFirst(), "Не совпадают данные комментариев");

        verify(itemMapper, times(1)).toItemWithBookingAndCommentsDto(any(), any(), any(), anyList());
        verify(itemRepository, times(1)).findById(1);
        verify(bookingRepository, times(1)).findByItem_IdAndEndIsAfterOrderByEnd(anyLong(), any());
        verify(bookingRepository, times(1)).findByItem_IdAndStartIsBeforeOrderByStartDesc(anyLong(), any());
        verify(commentRepository, times(1)).findAllByItem_Id(1);
        verify(commentMapper, times(1)).toCommentDto(any());
        verifyNoMoreInteractions(itemMapper, itemRepository, bookingRepository, bookingMapper, commentMapper, commentRepository);
    }

    @Test
    void getByIdShouldReturnDtoWhenInvoked() {
        when(itemRepository.findById(1)).thenReturn(Optional.of(items.getFirst()));
        when(itemMapper.toItemDto(items.getFirst())).thenReturn(itemsDto.getFirst());

        ItemDto resultDto = itemService.getById(1);

        assertEquals(itemsDto.getFirst(), resultDto, "Не совпадают данные DTO");
        verify(itemRepository, times(1)).findById(1);
        verify(itemMapper, times(1)).toItemDto(any());
        verifyNoMoreInteractions(itemMapper, itemRepository, bookingRepository, bookingMapper, commentMapper, commentRepository);
    }

    @Test
    void getByIdShouldThrowExceptionWhenItemNotFound() {
        when(itemRepository.findById(1)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> itemService.getById(1));
        assertEquals("Вещь 1 не найдена", exception.getMessage());
        verify(itemRepository, times(1)).findById(1);
        verifyNoMoreInteractions(itemMapper, itemRepository, bookingRepository, bookingMapper, commentMapper, commentRepository);
    }

    @Test
    void createShouldCreateItemWithNullRequest() {
        when(itemMapper.toItem(any())).thenReturn(items.getFirst());
        when(userService.getUserById(anyLong())).thenReturn(users.getFirst());

        itemService.create(1, itemsDto.getFirst());

        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item savedItem = itemArgumentCaptor.getValue();
        assertEquals(users.getFirst().getId(), savedItem.getOwner().getId(), "id владельца не совпадает");
        assertEquals(items.getFirst().getId(), savedItem.getId(), "id предмета не совпадает");
        assertNull(savedItem.getRequest());
        verify(itemMapper, times(1)).toItem(any());
        verify(userService, times(1)).getUserById(anyLong());
        verify(itemMapper,times(1)).toItemDto(any());
        verifyNoMoreInteractions(itemMapper, itemRepository, bookingRepository, bookingMapper, commentMapper, commentRepository);
    }

    @Test
    void createShouldCreateItemWithRequest() {
        when(itemMapper.toItem(any())).thenReturn(items.getFirst());
        when(userService.getUserById(anyLong())).thenReturn(users.getFirst());
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(requests.getFirst()));

        itemService.create(1, itemsDto.get(2));

        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item savedItem = itemArgumentCaptor.getValue();
        assertEquals(users.getFirst().getId(), savedItem.getOwner().getId(), "id владельца не совпадает");
        assertEquals(items.getFirst().getId(), savedItem.getId(), "id предмета не совпадает");
        assertEquals(items.getFirst().getRequest().getId(), savedItem.getRequest().getId(), "id запроса не совпадает");
        verify(itemMapper, times(1)).toItem(any());
        verify(userService, times(1)).getUserById(anyLong());
        verify(itemMapper,times(1)).toItemDto(any());
        verify(itemRequestRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(itemMapper, itemRepository, bookingRepository, bookingMapper, commentMapper, commentRepository);
    }

    @Test
    void updateShouldUpdateItemWhenInvokedForOwner() {
        when(itemRepository.findById(1)).thenReturn(Optional.of(items.getFirst()));

        itemService.update(2, 1, itemsDto.get(1));

        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item savedItem = itemArgumentCaptor.getValue();
        assertEquals(items.get(1).getName(), savedItem.getName(), "Имя предмета не было изменено");
        assertEquals(items.get(1).getDescription(),savedItem.getDescription(), "Описание предмета не было изменено");
        assertFalse(items.get(1).isAvailable(), "Доступность предмета не была изменена");
        verify(itemMapper,times(1)).toItemDto(any());
        verify(itemRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(itemMapper, itemRepository, bookingRepository, bookingMapper, commentMapper, commentRepository);
    }

    @Test
    void updateShouldUpdateItemAndNotUpdateNameWhenInvokedForOwnerWhenNameIsNull() {
        ItemDto itemDtoToUpdate = new ItemDto(null, null, "Second item desc", false, null);
        when(itemRepository.findById(1)).thenReturn(Optional.of(items.getFirst()));

        itemService.update(2, 1, itemDtoToUpdate);

        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item savedItem = itemArgumentCaptor.getValue();
        assertEquals(items.getFirst().getName(), savedItem.getName(), "Имя предмета было изменено ошибочно");
        assertEquals(itemDtoToUpdate.description(),savedItem.getDescription(), "Описание предмета не было изменено");
        assertFalse(savedItem.isAvailable(), "Доступность предмета не была изменена");
        verify(itemMapper,times(1)).toItemDto(any());
        verify(itemRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(itemMapper, itemRepository, bookingRepository, bookingMapper, commentMapper, commentRepository);
    }

    @Test
    void updateShouldUpdateItemAndNotUpdateDescriptionWhenInvokedForOwnerAndDescriptionIsNull() {
        ItemDto itemDtoToUpdate = new ItemDto(null, "Second item", null, false, null);
        when(itemRepository.findById(1)).thenReturn(Optional.of(items.getFirst()));

        itemService.update(2, 1, itemDtoToUpdate);

        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item savedItem = itemArgumentCaptor.getValue();
        assertEquals(itemDtoToUpdate.name(), savedItem.getName(), "Имя предмета не было изменено");
        assertEquals(items.getFirst().getDescription(),savedItem.getDescription(), "Описание предмета было изменено ошибочно");
        assertFalse(savedItem.isAvailable(), "Доступность предмета не была изменена");
        verify(itemMapper,times(1)).toItemDto(any());
        verify(itemRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(itemMapper, itemRepository, bookingRepository, bookingMapper, commentMapper, commentRepository);
    }

    @Test
    void updateShouldUpdateItemAndNotUpdateAvailableWhenInvokedForOwnerAndAvailableIsNull() {
        ItemDto itemDtoToUpdate = new ItemDto(null, "Second item", "Second item desc", null, null);
        when(itemRepository.findById(1)).thenReturn(Optional.of(items.getFirst()));

        itemService.update(2, 1, itemDtoToUpdate);

        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item savedItem = itemArgumentCaptor.getValue();
        assertEquals(items.get(1).getName(), savedItem.getName(), "Имя предмета не было изменено");
        assertEquals(items.get(1).getDescription(),savedItem.getDescription(), "Описание предмета не было изменено");
        assertTrue(savedItem.isAvailable(), "Доступность предмета была изменена ошибочно");
        verify(itemMapper,times(1)).toItemDto(any());
        verify(itemRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(itemMapper, itemRepository, bookingRepository, bookingMapper, commentMapper, commentRepository);
    }

    @Test
    void updateShouldThrowExceptionWhenInvokedForNotOwner() {
        when(itemRepository.findById(1)).thenReturn(Optional.of(items.getFirst()));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> itemService.update(1, 1, itemsDto.get(1)));

        verify(itemRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(itemMapper, itemRepository, bookingRepository, bookingMapper, commentMapper, commentRepository);
    }

    @Test
    void deleteShouldDeleteItemWhenInvoked() {
        when(itemRepository.findById(1)).thenReturn(Optional.of(items.getFirst()));

        itemService.delete(1);

        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).delete(items.getFirst());
        verifyNoMoreInteractions(itemMapper, itemRepository, bookingRepository, bookingMapper, commentMapper, commentRepository);
    }

    @Test
    void searchAvailableShouldReturnItemDtoListWhenFoundData() {
        when(itemRepository.search(anyString(), anyBoolean())).thenReturn(List.of(items.getFirst()));
        when(itemMapper.toItemDto(any())).thenReturn(itemsDto.getFirst());

        List<ItemDto> resultDtoList = itemService.searchAvailable("First");

        assertEquals(1, resultDtoList.size(), "Размер спска должен бытьравен 1");
        assertEquals(itemsDto.getFirst(), resultDtoList.getFirst(), "Полученые данные не соответствуют ожидаемым");
        verify(itemRepository, times(1)).search("First", true);
        verify(itemMapper, times(1)).toItemDto(items.getFirst());
        verifyNoMoreInteractions(itemMapper, itemRepository, bookingRepository, bookingMapper, commentMapper, commentRepository);
    }

    @Test
    void searchAvailableShouldReturnEmptyListWhenSearchStringIsEmpty() {
        List<ItemDto> resultDtoList = itemService.searchAvailable("");

        assertTrue(resultDtoList.isEmpty());
        verifyNoInteractions(itemMapper, itemRepository, bookingRepository, bookingMapper, commentMapper, commentRepository);
    }

    @Test
    void getItemByIdShouldThrowExceptionWhenNotFound() {
        when(itemRepository.findById(1)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.getItemById(1));

        assertEquals("Вещь 1 не найдена", exception.getMessage());
        verify(itemRepository, times(1)).findById(1);
        verifyNoMoreInteractions(itemMapper, itemRepository, bookingRepository, bookingMapper, commentMapper, commentRepository);
    }

    @Test
    void addCommentShouldAddCommentWhenInvoked() {
        when(userService.getUserById(1)).thenReturn(users.getFirst());
        when(itemRepository.findById(1)).thenReturn(Optional.of(items.getFirst()));
        when(bookingRepository.findAllByBooker_IdAndEndIsBefore(anyLong(), any(), any())).thenReturn(List.of(bookings.getFirst()));

        itemService.addComment(1, 1, "Test comment");

        verify(commentRepository).save(commentArgumentCaptor.capture());
        Comment savedComment = commentArgumentCaptor.getValue();
        assertEquals(items.getFirst().getId(), savedComment.getItem().getId(), "Неверный id предмета в сохраняемом комментарии");
        assertEquals(users.getFirst().getName(), savedComment.getAuthor().getName(), "Неверное имя автора в сохраняемом комментарии");
        assertEquals("Test comment", savedComment.getText(), "Неверный текст в сохраняемом комментарии");

        verify(userService, times(1)).getUserById(1);
        verify(itemRepository, times(1)).findById(1);
        verify(bookingRepository, times(1)).findAllByBooker_IdAndEndIsBefore(anyLong(), any(), any());
        verify(commentMapper, times(1)).toCommentDto(any());
        verifyNoMoreInteractions(itemMapper, itemRepository, bookingRepository, bookingMapper, commentMapper, commentRepository);
    }

    @Test
    void addCommentShouldThrowExceptionWhenNoBookingsFound() {
        when(userService.getUserById(1)).thenReturn(users.getFirst());
        when(itemRepository.findById(2)).thenReturn(Optional.of(items.get(1)));
        when(bookingRepository.findAllByBooker_IdAndEndIsBefore(anyLong(), any(), any())).thenReturn(List.of(bookings.getFirst()));

        InvalidAddCommentRequestException exception = assertThrows(InvalidAddCommentRequestException.class,
                () -> itemService.addComment(2, 1, "Test comment"));

        verify(userService, times(1)).getUserById(1);
        verify(itemRepository, times(1)).findById(2);
        verify(bookingRepository, times(1)).findAllByBooker_IdAndEndIsBefore(anyLong(), any(), any());
        verifyNoMoreInteractions(itemMapper, itemRepository, bookingRepository, bookingMapper, commentMapper, commentRepository);
    }
}