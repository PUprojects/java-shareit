package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {
    private final CommentMapper commentMapper = new CommentMapperImpl();

    @Test
    void toCommentDtoShouldMapCommentToDto() {
        User user = new User(1, "First user", "first@mail.ru");
        Item item = new Item(1, "First item", "First item desc",
                true, user, null);
        Comment comment = new Comment();
        comment.setId(1);
        comment.setText("Test text");
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());

        CommentDto commentDto = commentMapper.toCommentDto(comment);

        assertEquals(comment.getId(), commentDto.id(), "id не совпадает");
        assertEquals(comment.getText(), commentDto.text(), "Текст не совпадает");
        assertEquals(comment.getItem().getId(), commentDto.itemId(), "id предмета не совпадает");
        assertEquals(comment.getAuthor().getName(), commentDto.authorName(), "Имя автора не совпадает");
        assertEquals(comment.getCreated().toString(), commentDto.created().toString(), "Время создания не совпадает");
    }

    @Test
    void toCommentDtoShouldReturnNullWhenCommentIsNull() {
        assertNull(commentMapper.toCommentDto(null));
    }
}