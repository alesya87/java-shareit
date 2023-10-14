package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingAddDto;
import ru.practicum.shareit.booking.dto.BookingLogDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              ItemRepository itemRepository,
                              UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public BookingLogDto addBooking(Long userId, BookingAddDto bookingAddDto) {
        log.debug("Сервис - добавление бронирования");
        LocalDateTime currentDateTime = LocalDateTime.now();
        if (bookingAddDto.getEnd().isBefore(bookingAddDto.getStart()) ||
                bookingAddDto.getEnd().equals(bookingAddDto.getStart()) ||
                bookingAddDto.getStart().isBefore(currentDateTime)) {
            throw new IncorrectTimeDateException("Ошибка даты начала " + bookingAddDto.getStart() +
                    " и конца аренды " + bookingAddDto.getEnd());
        }

        Item item = itemRepository.findById(bookingAddDto.getItemId()).orElse(null);
        User booker = userRepository.findById(userId).orElse(null);

        if (item == null) {
            throw new EntityNotFoundException("Вещи с id " + bookingAddDto.getItemId() + " не существует");
        }

        if (booker == null) {
            throw new EntityNotFoundException("Пользователя с id " + userId + " не существует");
        }

        if (item.getOwnerId() == userId) {
            throw new EntityAccessException("Пользователь не может забронировать свой предмет");
        }

        if (!item.getAvailable()) {
            throw new EntityNotAvailableException("Вещь с id " + bookingAddDto.getItemId() +
                    " не доступна к бронированию");
        }

        Booking booking = Booking.builder()
                .start(bookingAddDto.getStart())
                .end(bookingAddDto.getEnd())
                .booker(booker)
                .item(item)
                .status(BookingStatus.WAITING)
                .build();

        return BookingMapper.mapToBookingLogDto(bookingRepository.save(booking));
    }

    @Override
    public BookingLogDto updateBookingStatus(Long userId, Boolean approved, Long bookingId) {
        log.debug("Сервис - изменение статуса бронирования с id {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId).orElse(null);

        if (booking == null) {
            throw new EntityNotFoundException("Бронирования с id " + bookingId + " не существует");
        }

        if (!Objects.equals(booking.getItem().getOwnerId(), userId)) {
            throw new EntityAccessException("Бронирование может подтвердить только владелец вещи");
        }

        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new DuplicateDataException("Бронирование с id " + bookingId + " уже подтверждено");
        }

        if (bookingRepository.existsByItem_IdAndStartBeforeAndEndAfter(booking.getItem().getId(),
                booking.getStart(), booking.getEnd())) {
            throw new EntityNotAvailableException("Вещь с id " + booking.getItem().getId() +
                    "  не доступна к бронированию на выбранное время");
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        return BookingMapper.mapToBookingLogDto(bookingRepository.save(booking));
    }

    @Override
    public BookingLogDto getBookingById(Long userId, Long bookingId) {
        log.debug("Сервис - получение бронирования с id {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId).orElse(null);

        if (booking == null) {
            throw new EntityNotFoundException("Бронирования с id " + bookingId + " не существует");
        }

        if (Objects.equals(booking.getBooker().getId(), userId)
                || Objects.equals(booking.getItem().getOwnerId(), userId)) {
            return BookingMapper.mapToBookingLogDto(booking);
        } else {
            throw new EntityNotFoundException(String.format("Просмотр бронирования item с id " + booking.getItem().getId() +
                    " доступно только для владельца"));
        }
    }

    @Override
    public List<BookingLogDto> getAllUserBookings(BookingStatus state, Long userId) {
        log.debug("Сервис - получение всех бронирований пользователя {} со статусом {}", userId, state);

        User booker = userRepository.findById(userId).orElse(null);
        if (booker == null) {
            throw new EntityNotFoundException("Пользователя с id " + userId + " не существует");
        }

        LocalDateTime currentDateTime = LocalDateTime.now();
        switch (state) {
            case ALL:
                return BookingMapper.mapToListBookingDto(bookingRepository
                        .findByBookerIdOrderByStartDesc(userId));
            case PAST:
                return BookingMapper.mapToListBookingDto(bookingRepository
                        .findByEndIsBeforeAndBookerIdOrderByStartDesc(currentDateTime, userId));
            case FUTURE:
                return BookingMapper.mapToListBookingDto(bookingRepository
                        .findByStartIsAfterAndBookerIdOrderByStartDesc(currentDateTime, userId));
            case CURRENT:
                return BookingMapper.mapToListBookingDto(bookingRepository
                        .findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                                currentDateTime, currentDateTime));
            case WAITING:
            case REJECTED:
                return BookingMapper.mapToListBookingDto(bookingRepository
                        .findByStatusAndBookerIdOrderByStartDesc(state, userId));
        }
        throw new UnsupportedStatusException("Unknown state: " + state);
    }

    @Override
    public List<BookingLogDto> getAllItemBookingsUser(Long userId, BookingStatus state) {
        log.debug("Сервис - получение всех бронирований ползователя {} со статусом {}", userId, state);

        User booker = userRepository.findById(userId).orElse(null);
        if (booker == null) {
            throw new EntityNotFoundException("Пользователя с id " + userId + " не существует");
        }

        List<Booking> bookings = new ArrayList<>();
        LocalDateTime currentDateTime = LocalDateTime.now();
        switch (state) {
            case ALL:
                return BookingMapper.mapToListBookingDto(bookingRepository
                        .findByItem_OwnerIdOrderByStartDesc(userId));
            case PAST:
                return BookingMapper.mapToListBookingDto(bookingRepository
                        .findByItem_OwnerIdAndEndIsBeforeOrderByStartDesc(userId, currentDateTime));
            case FUTURE:
                return BookingMapper.mapToListBookingDto(bookingRepository
                        .findByItem_OwnerIdAndEndIsAfterOrderByStartDesc(userId, currentDateTime));
            case CURRENT:
                return BookingMapper.mapToListBookingDto(bookingRepository
                        .findByItem_OwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                                currentDateTime, currentDateTime));
            case WAITING:
            case REJECTED:
                return BookingMapper.mapToListBookingDto(bookingRepository
                        .findByItem_OwnerIdAndStatusOrderByStartDesc(userId, state));
        }
        throw new UnsupportedStatusException("Unknown state: " + state);
    }
}
