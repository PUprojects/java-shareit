package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
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
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public List<ItemDto> getAllByUser(long userId) {
        return itemRepository.findByOwner(userId).stream()
                .map(itemMapper::toItemDto)
                .toList();
    }

    @Override
    public ItemWithBookingAndCommentsDto getByIdWithBookingsAndComments(long id) {
        Item item = getItemById(id);

        Booking lastBooking = bookingRepository.findByItem_IdAndStartIsBeforeOrderByStartDesc(item.getOwner().getId(),
                LocalDateTime.now());
        BookingDto lastBookingDto = (lastBooking == null) ? null : bookingMapper.toBookingDto(lastBooking);

        Booking nextBooking = bookingRepository.findByItem_IdAndEndIsAfterOrderByEnd(item.getOwner().getId(),
                LocalDateTime.now());
        BookingDto nextBookingDto = (nextBooking == null) ? null : bookingMapper.toBookingDto(nextBooking);

        List<CommentDto> comments = commentRepository.findAllByItem_Id(id).stream()
                .map(commentMapper::toCommentDto)
                .toList();

        return itemMapper.toItemWithBookingAndCommentsDto(item, lastBookingDto, nextBookingDto, comments);
    }

    @Override
    public ItemDto getById(long id) {
        return itemMapper.toItemDto(itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь " + id + " не найдена")));
    }

    @Override
    public ItemDto create(long ownerId, ItemDto itemDto) {
        Item item = itemMapper.toItem(itemDto);
        User user = userService.getUserById(ownerId);
        item.setOwner(user);
        if (itemDto.requestId() != null) {
            itemRequestRepository.findById(itemDto.requestId())
                    .ifPresent(item::setRequest);
        }
        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(long userId, long itemId, ItemDto itemDto) {
        Item item = getItemById(itemId);

        if (item.getOwner().getId() != userId) {
            throw new AccessDeniedException("Пользователь " + userId + " не является хозяином вещи");
        }

        if (itemDto.name() != null) {
            item.setName(itemDto.name());
        }

        if (itemDto.description() != null) {
            item.setDescription(itemDto.description());
        }

        if (itemDto.available() != null) {
            item.setAvailable(itemDto.available());
        }

        itemRepository.save(item);
        return itemMapper.toItemDto(item);
    }

    @Override
    public void delete(long id) {
        Item item = getItemById(id);
        itemRepository.delete(item);
    }

    @Override
    public List<ItemDto> searchAvailable(String searchString) {
        if (searchString.isEmpty())
            return List.of();
        return itemRepository.search(searchString, true).stream()
                .map(itemMapper::toItemDto)
                .toList();
    }

    @Override
    public Item getItemById(long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь " + id + " не найдена"));
    }

    @Override
    public CommentDto addComment(long itemId, long authorId, String text) {
        User author = userService.getUserById(authorId);
        Item item = getItemById(itemId);

        bookingRepository.findAllByBooker_IdAndEndIsBefore(author.getId(), LocalDateTime.now(), Sort.by("end"))
                .stream()
                .filter(booking -> booking.getItem().getId() == item.getId())
                .findFirst()
                .orElseThrow(() -> new InvalidAddCommentRequestException("У пользователя " + author.getId() +
                        " нет завершённых бронирований вещи " + item.getId()));

        Comment comment = new Comment();
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        return commentMapper.toCommentDto(commentRepository.save(comment));
    }
}
