package ru.practicum.shareit.booking.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;

import ru.practicum.shareit.booking.dto.BookingAddDto;
import ru.practicum.shareit.booking.dto.BookingLogDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@Validated
@Slf4j
public class BookingController {
    private final BookingService bookingService;
    private static final String OWNER_HEADER = "X-Sharer-User-Id";

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingLogDto postBookings(@Valid @RequestHeader(OWNER_HEADER) @NotNull Long userId,
                                      @Valid @RequestBody BookingAddDto bookingAddDto) {
        log.debug("Поступил запрос на создание бронирования item с id {} для ползователя с id {}.",
                bookingAddDto.getItemId(), userId);
        return bookingService.addBooking(userId, bookingAddDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingLogDto patchApproved(@Valid @RequestHeader(OWNER_HEADER) @NotNull Long userId,
                                       @Valid @RequestParam @NotNull Boolean approved,
                                       @Valid @NotNull @PathVariable Long bookingId) {
        log.debug("Поступил запрос на одобрение(отклонение) бронирования с id {}", bookingId);
        return bookingService.updateBookingStatus(userId, approved, bookingId);
    }

    @GetMapping("/{bookingId}")
    public BookingLogDto getBookingById(@Valid @RequestHeader(OWNER_HEADER) @NotNull Long userId,
                                        @Valid @PathVariable @NotNull Long bookingId) {
        log.debug("Поступил запрос на получение бронирования с id {}.", bookingId);
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingLogDto> getAllUserBookings(@Valid @RequestParam(defaultValue = "ALL") @NotNull BookingStatus state,
                                                  @RequestHeader(OWNER_HEADER) @Valid @NotNull Long userId,
                                                  @Valid @RequestParam(defaultValue = "0") @Min(value = 0) int from,
                                                  @Valid @RequestParam(defaultValue = "10") @Min(value = 1) int size) {
        log.debug("Поступил запрос на получение всех бронирований пользователя {} со статусом {}", userId, state);
        return bookingService.getAllUserBookings(state, userId, from, size);
    }

    @GetMapping("/owner")
    public List<BookingLogDto> getAllItemBookingsUser(@Valid @RequestHeader(OWNER_HEADER) @NotNull Long userId,
                                                      @RequestParam(defaultValue = "ALL") BookingStatus state,
                                                      @Valid @RequestParam(defaultValue = "0") @Min(value = 0) int from,
                                                      @Valid @RequestParam(defaultValue = "10") @Min(value = 1) int size) {
        log.debug("Поступил запрос на получение всех бронировании от пользователя {} со статусом {}", userId, state);
        return bookingService.getAllItemBookingsUser(userId, state, from, size);
    }
}
