package ru.practicum.shareit.booking.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingAddDto;
import ru.practicum.shareit.booking.dto.BookingLogDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@Slf4j
public class BookingController {
    private final BookingService bookingService;
    private static final String OWNER_HEADER = "X-Sharer-User-Id";

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingLogDto addBooking(@RequestHeader(OWNER_HEADER) Long userId,
                                    @RequestBody BookingAddDto bookingAddDto) {
        log.debug("Поступил запрос на создание бронирования item с id {} для ползователя с id {}.",
                bookingAddDto.getItemId(), userId);
        return bookingService.addBooking(userId, bookingAddDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingLogDto updateBookingStatus(@RequestHeader(OWNER_HEADER) Long userId,
                                             @RequestParam Boolean approved,
                                             @PathVariable Long bookingId) {
        log.debug("Поступил запрос на одобрение(отклонение) бронирования с id {}", bookingId);
        return bookingService.updateBookingStatus(userId, approved, bookingId);
    }

    @GetMapping("/{bookingId}")
    public BookingLogDto getBookingById(@RequestHeader(OWNER_HEADER) Long userId,
                                        @PathVariable Long bookingId) {
        log.debug("Поступил запрос на получение бронирования с id {}.", bookingId);
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingLogDto> getAllUserBookings(@RequestParam(defaultValue = "ALL") String state,
                                                  @RequestHeader(OWNER_HEADER) Long userId,
                                                  @RequestParam(defaultValue = "0") int from,
                                                  @RequestParam(defaultValue = "10") int size) {
        log.debug("Поступил запрос на получение всех бронирований пользователя {} со статусом {}", userId, state);
        return bookingService.getAllUserBookings(BookingStatus.valueOf(state), userId, from, size);
    }

    @GetMapping("/owner")
    public List<BookingLogDto> getAllItemBookingsUser(@RequestHeader(OWNER_HEADER) Long userId,
                                                      @RequestParam(defaultValue = "ALL") String state,
                                                      @RequestParam(defaultValue = "0") int from,
                                                      @RequestParam(defaultValue = "10") int size) {
        log.debug("Поступил запрос на получение всех бронировании от пользователя {} со статусом {}", userId, state);
        return bookingService.getAllItemBookingsUser(userId, BookingStatus.valueOf(state), from, size);
    }
}
