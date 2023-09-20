package ru.practicum.shareit.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(final String message) {
        super(message);
    }
}
