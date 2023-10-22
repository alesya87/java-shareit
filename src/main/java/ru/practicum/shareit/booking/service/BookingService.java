package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingAddDto;
import ru.practicum.shareit.booking.dto.BookingLogDto;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.util.List;

public interface BookingService {
    BookingLogDto addBooking(Long userId, BookingAddDto bookingAddDto);

    BookingLogDto updateBookingStatus(Long userId, Boolean approved, Long bookingId);

    BookingLogDto getBookingById(Long userId, Long bookingId);

    List<BookingLogDto> getAllUserBookings(BookingStatus state, Long userId, int from, int size);

    List<BookingLogDto> getAllItemBookingsUser(Long userId, BookingStatus state, int from, int size);
}
