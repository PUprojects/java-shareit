package ru.practicum.shareit.exception;

public class InvalidAddCommentRequestException extends RuntimeException {
    public InvalidAddCommentRequestException(String message) {
        super(message);
    }
}
