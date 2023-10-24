package ru.practicum.shareit.exception;

public class IncorrectEmailException extends RuntimeException {
    public IncorrectEmailException(final String message) {
        super(message);
    }
}
