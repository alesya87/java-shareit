package ru.practicum.shareit.exception;

public class IncorrectTimeDateException extends RuntimeException {
    public IncorrectTimeDateException(final String message) {
        super(message);
    }
}
