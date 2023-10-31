package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
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
    @Transactional
    public BookingLogDto addBooking(Long userId, BookingAddDto bookingAddDto) {
        log.debug("Сервис - добавление бронирования");
        LocalDateTime currentDateTime = LocalDateTime.now();
        if (bookingAddDto.getEnd().isBefore(bookingAddDto.getStart()) ||
                bookingAddDto.getEnd().equals(bookingAddDto.getStart()) ||
                bookingAddDto.getStart().isBefore(currentDateTime)) {
            throw new IncorrectTimeDateException("Ошибка даты начала " + bookingAddDto.getStart() +
                    " и конца аренды " + bookingAddDto.getEnd());
        }

        Item item = itemRepository.findById(bookingAddDto.getItemId()).orElseThrow(() ->
                new EntityNotFoundException("Вещи с id " + bookingAddDto.getItemId() + " не существует"));

        User booker = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Пользователя с id " + userId + " не существует"));

        if (Objects.equals(item.getOwner().getId(), userId)) {
            throw new EntityAccessException("Пользователь не может забронировать свой предмет");
        }

        if (!item.getAvailable()) {
            throw new EntityNotAvailableException("Вещь с id " + bookingAddDto.getItemId() +
                    " не доступна к бронированию");
        }

        Booking booking = BookingMapper.mapToBooking(bookingAddDto, booker, item);

        return BookingMapper.mapToBookingLogDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingLogDto updateBookingStatus(Long userId, Boolean approved, Long bookingId) {
        log.debug("Сервис - изменение статуса бронирования с id {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new EntityNotFoundException("Бронирования с id " + bookingId + " не существует"));

        if (!Objects.equals(booking.getItem().getOwner().getId(), userId)) {
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
    @Transactional(readOnly = true)
    public BookingLogDto getBookingById(Long userId, Long bookingId) {
        log.debug("Сервис - получение бронирования с id {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new EntityNotFoundException("Бронирования с id " + bookingId + " не существует"));

        if (Objects.equals(booking.getBooker().getId(), userId)
                || Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            return BookingMapper.mapToBookingLogDto(booking);
        } else {
            throw new EntityAccessException(String.format("Просмотр бронирования item с id " + booking.getItem().getId() +
                    " доступно только для владельца"));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingLogDto> getAllUserBookings(BookingStatus state, Long userId, int from, int size) {
        log.debug("Сервис - получение всех бронирований пользователя {} со статусом {}", userId, state);

        User booker = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Пользователя с id " + userId + " не существует"));

        Sort sort = Sort.by(Sort.Order.desc("start"));
        Pageable pageable = PageRequest.of(from / size, size, sort);

        LocalDateTime currentDateTime = LocalDateTime.now();
        switch (state) {
            case ALL:
                return BookingMapper.mapToListBookingDto(bookingRepository
                        .findByBookerId(userId, pageable));
            case PAST:
                return BookingMapper.mapToListBookingDto(bookingRepository
                        .findByEndIsBeforeAndBookerId(currentDateTime, userId, pageable));
            case FUTURE:
                return BookingMapper.mapToListBookingDto(bookingRepository
                        .findByStartIsAfterAndBookerId(currentDateTime, userId, pageable));
            case CURRENT:
                return BookingMapper.mapToListBookingDto(bookingRepository
                        .findByBookerIdAndStartBeforeAndEndAfter(userId,
                                currentDateTime, currentDateTime, pageable));
            case WAITING:
            case REJECTED:
                return BookingMapper.mapToListBookingDto(bookingRepository
                        .findByStatusAndBookerId(state, userId, pageable));
        }
        throw new UnsupportedStatusException("Unknown state: " + state);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingLogDto> getAllItemBookingsUser(Long userId, BookingStatus state, int from, int size) {
        log.debug("Сервис - получение всех бронирований ползователя {} со статусом {}", userId, state);

        User booker = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Пользователя с id " + userId + " не существует"));

        Sort sort = Sort.by(Sort.Order.desc("start"));
        Pageable pageable = PageRequest.of(from / size, size, sort);

        LocalDateTime currentDateTime = LocalDateTime.now();
        switch (state) {
            case ALL:
                return BookingMapper.mapToListBookingDto(bookingRepository
                        .findByItem_OwnerId(userId, pageable));
            case PAST:
                return BookingMapper.mapToListBookingDto(bookingRepository
                        .findByItem_OwnerIdAndEndIsBefore(userId, currentDateTime, pageable));
            case FUTURE:
                return BookingMapper.mapToListBookingDto(bookingRepository
                        .findByItem_OwnerIdAndEndIsAfter(userId, currentDateTime, pageable));
            case CURRENT:
                return BookingMapper.mapToListBookingDto(bookingRepository
                        .findByItem_OwnerIdAndStartBeforeAndEndAfter(userId,
                                currentDateTime, currentDateTime, pageable));
            case WAITING:
            case REJECTED:
                return BookingMapper.mapToListBookingDto(bookingRepository
                        .findByItem_OwnerIdAndStatus(userId, state, pageable));
        }
        throw new UnsupportedStatusException("Unknown state: " + state);
    }
}
