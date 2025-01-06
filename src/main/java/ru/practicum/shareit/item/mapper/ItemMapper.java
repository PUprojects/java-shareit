package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingAndCommentsDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                null
        );
    }

    public static Item toItem(ItemDto itemDto) {
        Item item = new Item();
        item.setId(itemDto.getId() == null ? 0 : item.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(null);
        item.setRequest(null);
        return item;
    }

    public static ItemWithBookingAndCommentsDto toItemWithBookingAndCommentsDto(Item item, BookingDto lastBooking,
                                                                                BookingDto nextBooking,
                                                                                List<CommentDto> comments) {
        return new ItemWithBookingAndCommentsDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                null,
                lastBooking,
                nextBooking,
                comments);
    }
}
