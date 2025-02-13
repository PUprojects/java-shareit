package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {
    @Mapping(target = "itemId", expression  = "java(comment.getItem().getId())")
    @Mapping(target = "authorName", expression  = "java(comment.getAuthor().getName())")
    CommentDto toCommentDto(Comment comment);
}
