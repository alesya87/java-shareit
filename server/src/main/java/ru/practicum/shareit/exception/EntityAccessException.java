package ru.practicum.shareit.exception;

public class EntityAccessException extends RuntimeException {
    public EntityAccessException(final String message) {
        super(message);
    }
}
