package ru.practicum.shareit.exception;

public class BookingNotAvailableException extends RuntimeException {
    public BookingNotAvailableException(final String message) {
        super(message);
    }
}
