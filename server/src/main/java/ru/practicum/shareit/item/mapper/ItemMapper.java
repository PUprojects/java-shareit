package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingAndCommentsDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemMapper {
    @Mapping(target = "requestId", source = "request.id")
    ItemDto toItemDto(Item item);

    @Mapping(target = "request", ignore = true)
    @Mapping(target = "owner", ignore = true)
    Item toItem(ItemDto itemDto);

    @Mapping(target = "requestId", source = "item.request.id")
    @Mapping(target = "id", source = "item.id")
    ItemWithBookingAndCommentsDto toItemWithBookingAndCommentsDto(Item item, BookingDto lastBooking,
                                                                  BookingDto nextBooking,
                                                                  List<CommentDto> comments);
}
