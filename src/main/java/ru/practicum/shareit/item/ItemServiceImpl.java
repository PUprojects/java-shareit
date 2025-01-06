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

    @Override
    public List<ItemDto> getAllByUser(long userId) {
        return itemRepository.findByOwner(userId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public ItemWithBookingAndCommentsDto getByIdWithBookingsAndComments(long id) {
        Item item = getItemById(id);

        Booking lastBooking = bookingRepository.findByItem_IdAndStartIsBeforeOrderByStartDesc(item.getOwner().getId(),
                LocalDateTime.now());
        BookingDto lastBookingDto = (lastBooking == null) ? null : BookingMapper.toBookingDto(lastBooking);

        Booking nextBooking = bookingRepository.findByItem_IdAndEndIsAfterOrderByEnd(item.getOwner().getId(),
                LocalDateTime.now());
        BookingDto nextBookingDto = (nextBooking == null) ? null : BookingMapper.toBookingDto(nextBooking);

        List<CommentDto> comments = commentRepository.findAllByItem_Id(id).stream()
                .map(CommentMapper::toCommentDto)
                .toList();

        return ItemMapper.toItemWithBookingAndCommentsDto(item, lastBookingDto, nextBookingDto, comments);
    }

    @Override
    public ItemDto getById(long id) {
        return ItemMapper.toItemDto(itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь " + id + " не найдена")));
    }

    @Override
    public ItemDto create(long ownerId, ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        User user = userService.getUserById(ownerId);
        item.setOwner(user);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(long userId, long itemId, ItemDto itemDto) {
        Item item = getItemById(itemId);

        if (item.getOwner().getId() != userId) {
            throw new AccessDeniedException("Пользователь " + userId + " не является хозяином вещи");
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        itemRepository.save(item);
        return ItemMapper.toItemDto(item);
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
                .map(ItemMapper::toItemDto)
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

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }
}
