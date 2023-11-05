package ru.practicum.shareit.exception;

public class DuplicateDataException extends RuntimeException {
    public DuplicateDataException(final String message) {
        super(message);
    }
}
