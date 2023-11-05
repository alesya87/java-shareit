package ru.practicum.shareit.booking.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.dto.BookingAddDto;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.exception.UnsupportedStatusException;


import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping(path = "/bookings")
@Validated
@Slf4j
public class BookingController {
    private final BookingClient bookingClient;
    private static final String OWNER_HEADER = "X-Sharer-User-Id";

    public BookingController(BookingClient bookingClient) {
        this.bookingClient = bookingClient;
    }

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader(OWNER_HEADER) Long userId,
                                             @Valid @RequestBody BookingAddDto bookingAddDto) {
        log.debug("Поступил запрос на создание бронирования item с id {} для ползователя с id {}.",
                bookingAddDto.getItemId(), userId);
        return bookingClient.addBooking(userId, bookingAddDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateBookingStatus(@RequestHeader(OWNER_HEADER) Long userId,
                                                      @Valid @RequestParam @NotNull Boolean approved,
                                                      @PathVariable Long bookingId) {
        log.debug("Поступил запрос на одобрение(отклонение) бронирования с id {}", bookingId);
        return bookingClient.updateBookingStatus(userId, approved, bookingId);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader(OWNER_HEADER) Long userId,
                                                 @PathVariable Long bookingId) {
        log.debug("Поступил запрос на получение бронирования с id {}.", bookingId);
        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUserBookings(@RequestParam(defaultValue = "ALL") String state,
                                                  @RequestHeader(OWNER_HEADER) Long userId,
                                                  @Valid @RequestParam(defaultValue = "0") @Min(value = 0) int from,
                                                  @Valid @RequestParam(defaultValue = "10") @Min(value = 1) int size) {
        log.debug("Поступил запрос на получение всех бронирований пользователя {} со статусом {}", userId, state);
        return bookingClient.getAllUserBookings(getBookingStatusFromString(state), userId, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllItemBookingsUser(@RequestHeader(OWNER_HEADER) Long userId,
                                                      @RequestParam(defaultValue = "ALL") String state,
                                                      @Valid @RequestParam(defaultValue = "0") @Min(value = 0) int from,
                                                      @Valid @RequestParam(defaultValue = "10") @Min(value = 1) int size) {
        log.debug("Поступил запрос на получение всех бронировании от пользователя {} со статусом {}", userId, state);
        return bookingClient.getAllItemBookingsUser(userId, getBookingStatusFromString(state), from, size);
    }

    private static BookingStatus getBookingStatusFromString(String value) {
        for (BookingStatus status : BookingStatus.values()) {
            if (status.toString().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new UnsupportedStatusException("Unknown state: " + value);
    }
}
