package ru.practicum.shareit.exception;

public class EntityNotAvailableException extends RuntimeException {
    public EntityNotAvailableException(final String message) {
        super(message);
    }
}
