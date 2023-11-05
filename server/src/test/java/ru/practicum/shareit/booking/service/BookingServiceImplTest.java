package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingAddDto;
import ru.practicum.shareit.booking.dto.BookingLogDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    private BookingService bookingService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, itemRepository, userRepository);
    }

    @Test
    public void shouldAddBooking() {
        long userId = 1L;
        long itemId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(4);
        User user = new User(userId, "owner name", "owner@email.com");
        Item item = new Item(itemId, "item name", "item description", true, new User(), null, null,
                Collections.emptyList(), null);
        BookingAddDto bookingAddDtoLastBooking = new BookingAddDto(itemId, start, end);
        Booking booking = new Booking(1L, start, end, item, user, BookingStatus.WAITING);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingLogDto bookingLogDto = bookingService.addBooking(userId, bookingAddDtoLastBooking);

        assertNotNull(bookingLogDto);

        verify(itemRepository, times(1)).findById(itemId);
        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    public void shouldThrowIncorrectTimeDateExceptionWhenAddBookingIfEndIsBeforeStart() {
        long userId = 1L;
        long itemId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(4);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingAddDto bookingAddDtoLastBooking = new BookingAddDto(itemId, start, end);

        Exception exception = assertThrows(IncorrectTimeDateException.class,
                () -> bookingService.addBooking(userId, bookingAddDtoLastBooking));

        assertEquals("Ошибка даты начала " + start +  " и конца аренды " + end,
                exception.getMessage());

        verify(itemRepository, never()).findById(anyLong());
        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void shouldThrowIncorrectTimeDateExceptionWhenAddBookingIfEndEqualsStart() {
        long userId = 1L;
        long itemId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(4);
        LocalDateTime end = start;
        BookingAddDto bookingAddDtoLastBooking = new BookingAddDto(itemId, start, end);

        Exception exception = assertThrows(IncorrectTimeDateException.class,
                () -> bookingService.addBooking(userId, bookingAddDtoLastBooking));

        assertEquals("Ошибка даты начала " + start +  " и конца аренды " + end,
                exception.getMessage());

        verify(itemRepository, never()).findById(anyLong());
        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void shouldThrowIncorrectTimeDateExceptionWhenAddBookingIfStartIsBeforeNow() {
        long userId = 1L;
        long itemId = 1L;
        LocalDateTime start = LocalDateTime.now().minusDays(4);
        LocalDateTime end = LocalDateTime.now().minusDays(2);
        BookingAddDto bookingAddDtoLastBooking = new BookingAddDto(itemId, start, end);

        Exception exception = assertThrows(IncorrectTimeDateException.class,
                () -> bookingService.addBooking(userId, bookingAddDtoLastBooking));

        assertEquals("Ошибка даты начала " + start +  " и конца аренды " + end,
                exception.getMessage());

        verify(itemRepository, never()).findById(anyLong());
        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionWhenAddBookingIfItemNotExist() {
        long userId = 1L;
        long itemId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(4);
        BookingAddDto bookingAddDtoLastBooking = new BookingAddDto(itemId, start, end);

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> bookingService.addBooking(userId, bookingAddDtoLastBooking));

        assertEquals("Вещи с id 1 не существует", exception.getMessage());

        verify(itemRepository, times(1)).findById(itemId);
        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionWhenAddBookingIfUserNotExist() {
        long userId = 1L;
        long itemId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(4);
        Item item = new Item(itemId, "item name", "item description", true, new User(), null, null,
                Collections.emptyList(), null);
        BookingAddDto bookingAddDtoLastBooking = new BookingAddDto(itemId, start, end);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> bookingService.addBooking(userId, bookingAddDtoLastBooking));

        assertEquals("Пользователя с id 1 не существует", exception.getMessage());

        verify(itemRepository, times(1)).findById(itemId);
        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void shouldThrowEntityAccessExceptionWhenAddBookingIfUserIsOwner() {
        long userId = 1L;
        long itemId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(4);
        User user = new User(userId, "owner name", "owner@email.com");
        Item item = new Item(itemId, "item name", "item description", true, user, null, null,
                Collections.emptyList(), null);
        BookingAddDto bookingAddDtoLastBooking = new BookingAddDto(itemId, start, end);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Exception exception = assertThrows(EntityAccessException.class,
                () -> bookingService.addBooking(userId, bookingAddDtoLastBooking));

        assertEquals("Пользователь не может забронировать свой предмет", exception.getMessage());

        verify(itemRepository, times(1)).findById(itemId);
        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void shouldThrowEntityAccessExceptionWhenAddBookingIfItemNotAvailable() {
        long userId = 1L;
        long itemId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(4);
        User user = new User(userId, "owner name", "owner@email.com");
        Item item = new Item(itemId, "item name", "item description", false, new User(), null, null,
                Collections.emptyList(), null);
        BookingAddDto bookingAddDtoLastBooking = new BookingAddDto(itemId, start, end);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Exception exception = assertThrows(EntityNotAvailableException.class,
                () -> bookingService.addBooking(userId, bookingAddDtoLastBooking));

        assertEquals("Вещь с id 1 не доступна к бронированию", exception.getMessage());

        verify(itemRepository, times(1)).findById(itemId);
        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void shouldUpdateBookingStatusApproved() {
        long userId = 1L;
        long bookingId = 1L;
        Boolean approved = true;
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(4);
        User user = new User(userId, "owner name", "owner@email.com");
        Item item = new Item(1L, "item name", "item description", true, user, null, null,
                Collections.emptyList(), null);
        Booking booking = new Booking(1L, start, end, item, user, BookingStatus.WAITING);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.existsByItem_IdAndStartBeforeAndEndAfter(booking.getItem().getId(),
                booking.getStart(), booking.getEnd())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingLogDto bookingLogDto = bookingService.updateBookingStatus(userId, approved, bookingId);

        assertNotNull(bookingLogDto);
        assertEquals(BookingStatus.APPROVED, bookingLogDto.getStatus());

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingRepository, times(1))
                .existsByItem_IdAndStartBeforeAndEndAfter(booking.getItem().getId(),
                booking.getStart(), booking.getEnd());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    public void shouldUpdateBookingStatusRejected() {
        long userId = 1L;
        long bookingId = 1L;
        Boolean approved = false;
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(4);
        User user = new User(userId, "owner name", "owner@email.com");
        Item item = new Item(1L, "item name", "item description", true, user, null, null,
                Collections.emptyList(), null);
        Booking booking = new Booking(1L, start, end, item, user, BookingStatus.WAITING);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.existsByItem_IdAndStartBeforeAndEndAfter(booking.getItem().getId(),
                booking.getStart(), booking.getEnd())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingLogDto bookingLogDto = bookingService.updateBookingStatus(userId, approved, bookingId);

        assertNotNull(bookingLogDto);
        assertEquals(BookingStatus.REJECTED, bookingLogDto.getStatus());

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingRepository, times(1))
                .existsByItem_IdAndStartBeforeAndEndAfter(booking.getItem().getId(),
                        booking.getStart(), booking.getEnd());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionWhenUpdateBookingStatusIfBookingNotExist() {
        long userId = 1L;
        long bookingId = 1L;
        Boolean approved = false;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> bookingService.updateBookingStatus(userId, approved, bookingId));

        assertEquals("Бронирования с id 1 не существует", exception.getMessage());

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingRepository, never())
                .existsByItem_IdAndStartBeforeAndEndAfter(anyLong(),
                        any(LocalDateTime.class), any(LocalDateTime.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void shouldThrowEntityAccessExceptionWhenUpdateBookingStatusIfOtherBooker() {
        long userId = 2L;
        long bookingId = 1L;
        Boolean approved = true;
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(4);
        User user = new User(1L, "owner name", "owner@email.com");
        Item item = new Item(1L, "item name", "item description", true, user, null, null,
                Collections.emptyList(), null);
        Booking booking = new Booking(1L, start, end, item, user, BookingStatus.WAITING);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        Exception exception = assertThrows(EntityAccessException.class,
                () -> bookingService.updateBookingStatus(userId, approved, bookingId));

        assertEquals("Бронирование может подтвердить только владелец вещи", exception.getMessage());

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingRepository, never())
                .existsByItem_IdAndStartBeforeAndEndAfter(anyLong(),
                        any(LocalDateTime.class), any(LocalDateTime.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void shouldThrowDuplicateDataExceptionWhenUpdateBookingStatusIsAlreadyApproved() {
        long userId = 1L;
        long bookingId = 1L;
        Boolean approved = true;
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(4);
        User user = new User(userId, "owner name", "owner@email.com");
        Item item = new Item(1L, "item name", "item description", true, user, null, null,
                Collections.emptyList(), null);
        Booking booking = new Booking(1L, start, end, item, user, BookingStatus.APPROVED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        Exception exception = assertThrows(DuplicateDataException.class,
                () -> bookingService.updateBookingStatus(userId, approved, bookingId));

        assertEquals("Бронирование с id 1 уже подтверждено", exception.getMessage());

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingRepository, never())
                .existsByItem_IdAndStartBeforeAndEndAfter(anyLong(),
                        any(LocalDateTime.class), any(LocalDateTime.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void shouldThrowEntityNotAvailableExceptionWhenUpdateBookingStatusIfItemBookedAtThisTime() {
        long userId = 1L;
        long bookingId = 1L;
        Boolean approved = false;
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(4);
        User user = new User(userId, "owner name", "owner@email.com");
        Item item = new Item(1L, "item name", "item description", true, user, null, null,
                Collections.emptyList(), null);
        Booking booking = new Booking(1L, start, end, item, user, BookingStatus.WAITING);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.existsByItem_IdAndStartBeforeAndEndAfter(booking.getItem().getId(),
                booking.getStart(), booking.getEnd())).thenReturn(true);

        Exception exception = assertThrows(EntityNotAvailableException.class,
                () -> bookingService.updateBookingStatus(userId, approved, bookingId));

        assertEquals("Вещь с id 1  не доступна к бронированию на выбранное время", exception.getMessage());

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingRepository, times(1))
                .existsByItem_IdAndStartBeforeAndEndAfter(booking.getItem().getId(),
                        booking.getStart(), booking.getEnd());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    public void shouldReturnBookingById() {
        long userId = 1L;
        long bookingId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(4);
        User user = new User(userId, "owner name", "owner@email.com");
        Item item = new Item(1L, "item name", "item description", true, user, null, null,
                Collections.emptyList(), null);
        Booking booking = new Booking(bookingId, start, end, item, user, BookingStatus.WAITING);
        BookingLogDto bookingLogDto = new BookingLogDto(bookingId, ItemMapper.mapToItemLogDto(item),
                UserMapper.mapToUserLogDto(user), start, end, BookingStatus.WAITING);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingLogDto result = bookingService.getBookingById(userId, bookingId);

        assertEquals(bookingLogDto, result);

        verify(bookingRepository, times(1)).findById(bookingId);
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionWhenGetBookingByIdIfBookingNotExist() {
        long userId = 1L;
        long bookingId = 1L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> bookingService.getBookingById(userId, bookingId));

        assertEquals("Бронирования с id 1 не существует", exception.getMessage());

        verify(bookingRepository, times(1)).findById(bookingId);
    }

    @Test
    public void shouldThrowEntityAccessExceptionWhenGetBookingByIdIfOtherBookingOwner() {
        long userId = 2L;
        long bookingId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(4);
        User user = new User(1L, "owner name", "owner@email.com");
        Item item = new Item(1L, "item name", "item description", true, user, null, null,
                Collections.emptyList(), null);
        Booking booking = new Booking(bookingId, start, end, item, user, BookingStatus.WAITING);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        Exception exception = assertThrows(EntityAccessException.class,
                () -> bookingService.getBookingById(userId, bookingId));

        assertEquals("Просмотр бронирования item с id 1 доступно только для владельца", exception.getMessage());

        verify(bookingRepository, times(1)).findById(bookingId);
    }

    @Test
    public void shouldReturnAllUserBookingsByStateAll() {
        BookingStatus bookingStatus = BookingStatus.ALL;
        long userId = 1L;
        int from = 0;
        int size = 10;
        LocalDateTime start = LocalDateTime.now().minusDays(3);
        LocalDateTime end = LocalDateTime.now().minusDays(2);
        User user = new User(userId, "owner name", "owner@email.com");
        Item item = new Item(1L, "item name", "item description", true, user, null, null,
                Collections.emptyList(), null);
        Booking booking = new Booking(1L, start, end, item, user, BookingStatus.PAST);
        BookingLogDto bookingLogDto = new BookingLogDto(1L, ItemMapper.mapToItemLogDto(item),
                UserMapper.mapToUserLogDto(user), start, end, BookingStatus.PAST);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerId(anyLong(), any(Pageable.class))).thenReturn(List.of(booking));

        List<BookingLogDto> result = bookingService.getAllUserBookings(bookingStatus, userId, from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(List.of(bookingLogDto), result);

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByEndIsBeforeAndBookerId(any(LocalDateTime.class),
                anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByStartIsAfterAndBookerId(any(LocalDateTime.class),
                anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByBookerIdAndStartBeforeAndEndAfter(anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByStatusAndBookerId(any(BookingStatus.class), anyLong(), any(Pageable.class));
    }

    @Test
    public void shouldReturnAllUserBookingsByStatePast() {
        BookingStatus bookingStatus = BookingStatus.PAST;
        long userId = 1L;
        int from = 0;
        int size = 10;
        LocalDateTime start = LocalDateTime.now().minusDays(3);
        LocalDateTime end = LocalDateTime.now().minusDays(2);
        User user = new User(userId, "owner name", "owner@email.com");
        Item item = new Item(1L, "item name", "item description", true, user, null, null,
                Collections.emptyList(), null);
        Booking booking = new Booking(1L, start, end, item, user, BookingStatus.PAST);
        BookingLogDto bookingLogDto = new BookingLogDto(1L, ItemMapper.mapToItemLogDto(item),
                UserMapper.mapToUserLogDto(user), start, end, BookingStatus.PAST);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByEndIsBeforeAndBookerId(any(LocalDateTime.class),anyLong(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingLogDto> result = bookingService.getAllUserBookings(bookingStatus, userId, from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(List.of(bookingLogDto), result);

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1)).findByEndIsBeforeAndBookerId(any(LocalDateTime.class),
                anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByStartIsAfterAndBookerId(any(LocalDateTime.class),
                anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByBookerIdAndStartBeforeAndEndAfter(anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByStatusAndBookerId(any(BookingStatus.class), anyLong(), any(Pageable.class));
    }

    @Test
    public void shouldReturnAllUserBookingsByStateFuture() {
        BookingStatus bookingStatus = BookingStatus.FUTURE;
        long userId = 1L;
        int from = 0;
        int size = 10;
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(4);
        User user = new User(userId, "owner name", "owner@email.com");
        Item item = new Item(1L, "item name", "item description", true, user, null, null,
                Collections.emptyList(), null);
        Booking booking = new Booking(1L, start, end, item, user, BookingStatus.FUTURE);
        BookingLogDto bookingLogDto = new BookingLogDto(1L, ItemMapper.mapToItemLogDto(item),
                UserMapper.mapToUserLogDto(user), start, end, BookingStatus.FUTURE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByStartIsAfterAndBookerId(any(LocalDateTime.class), anyLong(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingLogDto> result = bookingService.getAllUserBookings(bookingStatus, userId, from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(List.of(bookingLogDto), result);

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByEndIsBeforeAndBookerId(any(LocalDateTime.class),
                anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1)).findByStartIsAfterAndBookerId(any(LocalDateTime.class),
                anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByBookerIdAndStartBeforeAndEndAfter(anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByStatusAndBookerId(any(BookingStatus.class), anyLong(), any(Pageable.class));
    }

    @Test
    public void shouldReturnAllUserBookingsByStateCurrent() {
        BookingStatus bookingStatus = BookingStatus.CURRENT;
        long userId = 1L;
        int from = 0;
        int size = 10;
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        User user = new User(userId, "owner name", "owner@email.com");
        Item item = new Item(1L, "item name", "item description", true, user, null, null,
                Collections.emptyList(), null);
        Booking booking = new Booking(1L, start, end, item, user, BookingStatus.CURRENT);
        BookingLogDto bookingLogDto = new BookingLogDto(1L, ItemMapper.mapToItemLogDto(item),
                UserMapper.mapToUserLogDto(user), start, end, BookingStatus.CURRENT);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingLogDto> result = bookingService.getAllUserBookings(bookingStatus, userId, from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(List.of(bookingLogDto), result);

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByEndIsBeforeAndBookerId(any(LocalDateTime.class),
                anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByStartIsAfterAndBookerId(any(LocalDateTime.class),
                anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1)).findByBookerIdAndStartBeforeAndEndAfter(anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByStatusAndBookerId(any(BookingStatus.class), anyLong(), any(Pageable.class));
    }

    @Test
    public void shouldReturnAllUserBookingsByStateWaiting() {
        BookingStatus bookingStatus = BookingStatus.WAITING;
        long userId = 1L;
        int from = 0;
        int size = 10;
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        User user = new User(userId, "owner name", "owner@email.com");
        Item item = new Item(1L, "item name", "item description", true, user, null, null,
                Collections.emptyList(), null);
        Booking booking = new Booking(1L, start, end, item, user, BookingStatus.WAITING);
        BookingLogDto bookingLogDto = new BookingLogDto(1L, ItemMapper.mapToItemLogDto(item),
                UserMapper.mapToUserLogDto(user), start, end, BookingStatus.WAITING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByStatusAndBookerId(any(BookingStatus.class),
                        anyLong(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingLogDto> result = bookingService.getAllUserBookings(bookingStatus, userId, from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(List.of(bookingLogDto), result);

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByEndIsBeforeAndBookerId(any(LocalDateTime.class),
                anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByStartIsAfterAndBookerId(any(LocalDateTime.class),
                anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByBookerIdAndStartBeforeAndEndAfter(anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, times(1)).findByStatusAndBookerId(any(BookingStatus.class),
                anyLong(), any(Pageable.class));
    }

    @Test
    public void shouldReturnAllUserBookingsByStateRejected() {
        BookingStatus bookingStatus = BookingStatus.REJECTED;
        long userId = 1L;
        int from = 0;
        int size = 10;
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        User user = new User(userId, "owner name", "owner@email.com");
        Item item = new Item(1L, "item name", "item description", true, user, null, null,
                Collections.emptyList(), null);
        Booking booking = new Booking(1L, start, end, item, user, BookingStatus.REJECTED);
        BookingLogDto bookingLogDto = new BookingLogDto(1L, ItemMapper.mapToItemLogDto(item),
                UserMapper.mapToUserLogDto(user), start, end, BookingStatus.REJECTED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByStatusAndBookerId(any(BookingStatus.class),
                anyLong(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingLogDto> result = bookingService.getAllUserBookings(bookingStatus, userId, from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(List.of(bookingLogDto), result);

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByEndIsBeforeAndBookerId(any(LocalDateTime.class),
                anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByStartIsAfterAndBookerId(any(LocalDateTime.class),
                anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByBookerIdAndStartBeforeAndEndAfter(anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, times(1)).findByStatusAndBookerId(any(BookingStatus.class),
                anyLong(), any(Pageable.class));
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionWhenGetAllUserBookingsByStateIfUserNotExist() {
        BookingStatus bookingStatus = BookingStatus.REJECTED;
        long userId = 1L;
        int from = 0;
        int size = 10;

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                bookingService.getAllUserBookings(bookingStatus, userId, from, size));

        assertEquals("Пользователя с id 1 не существует", exception.getMessage());

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByEndIsBeforeAndBookerId(any(LocalDateTime.class),
                anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByStartIsAfterAndBookerId(any(LocalDateTime.class),
                anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByBookerIdAndStartBeforeAndEndAfter(anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByStatusAndBookerId(any(BookingStatus.class),
                anyLong(), any(Pageable.class));
    }

    @Test
    public void shouldReturnAllItemBookingsUserByStateAll() {
        BookingStatus bookingStatus = BookingStatus.ALL;
        long userId = 1L;
        int from = 0;
        int size = 10;
        LocalDateTime start = LocalDateTime.now().minusDays(3);
        LocalDateTime end = LocalDateTime.now().minusDays(2);
        User user = new User(userId, "owner name", "owner@email.com");
        Item item = new Item(1L, "item name", "item description", true, user, null, null,
                Collections.emptyList(), null);
        Booking booking = new Booking(1L, start, end, item, user, BookingStatus.PAST);
        BookingLogDto bookingLogDto = new BookingLogDto(1L, ItemMapper.mapToItemLogDto(item),
                UserMapper.mapToUserLogDto(user), start, end, BookingStatus.PAST);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByItem_OwnerId(anyLong(), any(Pageable.class))).thenReturn(List.of(booking));

        List<BookingLogDto> result = bookingService.getAllItemBookingsUser(userId, bookingStatus, from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(List.of(bookingLogDto), result);

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findByItem_OwnerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByItem_OwnerIdAndEndIsBefore(anyLong(), any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByItem_OwnerIdAndEndIsAfter(anyLong(), any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByItem_OwnerIdAndStartBeforeAndEndAfter(anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByItem_OwnerIdAndStatus(anyLong(), any(BookingStatus.class), any(Pageable.class));
    }

    @Test
    public void shouldReturnAllItemBookingsUserByStatePast() {
        BookingStatus bookingStatus = BookingStatus.PAST;
        long userId = 1L;
        int from = 0;
        int size = 10;
        LocalDateTime start = LocalDateTime.now().minusDays(3);
        LocalDateTime end = LocalDateTime.now().minusDays(2);
        User user = new User(userId, "owner name", "owner@email.com");
        Item item = new Item(1L, "item name", "item description", true, user, null, null,
                Collections.emptyList(), null);
        Booking booking = new Booking(1L, start, end, item, user, BookingStatus.PAST);
        BookingLogDto bookingLogDto = new BookingLogDto(1L, ItemMapper.mapToItemLogDto(item),
                UserMapper.mapToUserLogDto(user), start, end, BookingStatus.PAST);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByItem_OwnerIdAndEndIsBefore(anyLong(),
                any(LocalDateTime.class), any(Pageable.class))).thenReturn(List.of(booking));

        List<BookingLogDto> result = bookingService.getAllItemBookingsUser(userId, bookingStatus, from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(List.of(bookingLogDto), result);

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByItem_OwnerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1)).findByItem_OwnerIdAndEndIsBefore(anyLong(),
                any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByItem_OwnerIdAndEndIsAfter(anyLong(), any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByItem_OwnerIdAndStartBeforeAndEndAfter(anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByItem_OwnerIdAndStatus(anyLong(), any(BookingStatus.class), any(Pageable.class));
    }

    @Test
    public void shouldReturnAllItemBookingsUserByStateFuture() {
        BookingStatus bookingStatus = BookingStatus.FUTURE;
        long userId = 1L;
        int from = 0;
        int size = 10;
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(5);
        User user = new User(userId, "owner name", "owner@email.com");
        Item item = new Item(1L, "item name", "item description", true, user, null, null,
                Collections.emptyList(), null);
        Booking booking = new Booking(1L, start, end, item, user, BookingStatus.FUTURE);
        BookingLogDto bookingLogDto = new BookingLogDto(1L, ItemMapper.mapToItemLogDto(item),
                UserMapper.mapToUserLogDto(user), start, end, BookingStatus.FUTURE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByItem_OwnerIdAndEndIsAfter(anyLong(),
                any(LocalDateTime.class), any(Pageable.class))).thenReturn(List.of(booking));

        List<BookingLogDto> result = bookingService.getAllItemBookingsUser(userId, bookingStatus, from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(List.of(bookingLogDto), result);

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByItem_OwnerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByItem_OwnerIdAndEndIsBefore(anyLong(),
                any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, times(1)).findByItem_OwnerIdAndEndIsAfter(anyLong(),
                any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByItem_OwnerIdAndStartBeforeAndEndAfter(anyLong(), any(LocalDateTime.class),
                any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByItem_OwnerIdAndStatus(anyLong(), any(BookingStatus.class), any(Pageable.class));
    }

    @Test
    public void shouldReturnAllItemBookingsUserByStateCurrent() {
        BookingStatus bookingStatus = BookingStatus.CURRENT;
        long userId = 1L;
        int from = 0;
        int size = 10;
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        User user = new User(userId, "owner name", "owner@email.com");
        Item item = new Item(1L, "item name", "item description", true, user, null, null,
                Collections.emptyList(), null);
        Booking booking = new Booking(1L, start, end, item, user, BookingStatus.CURRENT);
        BookingLogDto bookingLogDto = new BookingLogDto(1L, ItemMapper.mapToItemLogDto(item),
                UserMapper.mapToUserLogDto(user), start, end, BookingStatus.CURRENT);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByItem_OwnerIdAndStartBeforeAndEndAfter(anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class))).thenReturn(List.of(booking));

        List<BookingLogDto> result = bookingService.getAllItemBookingsUser(userId, bookingStatus, from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(List.of(bookingLogDto), result);

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByItem_OwnerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByItem_OwnerIdAndEndIsBefore(anyLong(),
                any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByItem_OwnerIdAndEndIsAfter(anyLong(),
                any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, times(1)).findByItem_OwnerIdAndStartBeforeAndEndAfter(anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByItem_OwnerIdAndStatus(anyLong(), any(BookingStatus.class), any(Pageable.class));
    }

    @Test
    public void shouldReturnAllItemBookingsUserByStateWaiting() {
        BookingStatus bookingStatus = BookingStatus.WAITING;
        long userId = 1L;
        int from = 0;
        int size = 10;
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        User user = new User(userId, "owner name", "owner@email.com");
        Item item = new Item(1L, "item name", "item description", true, user, null, null,
                Collections.emptyList(), null);
        Booking booking = new Booking(1L, start, end, item, user, BookingStatus.WAITING);
        BookingLogDto bookingLogDto = new BookingLogDto(1L, ItemMapper.mapToItemLogDto(item),
                UserMapper.mapToUserLogDto(user), start, end, BookingStatus.WAITING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByItem_OwnerIdAndStatus(anyLong(),
                any(BookingStatus.class), any(Pageable.class))).thenReturn(List.of(booking));

        List<BookingLogDto> result = bookingService.getAllItemBookingsUser(userId, bookingStatus, from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(List.of(bookingLogDto), result);

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByItem_OwnerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByItem_OwnerIdAndEndIsBefore(anyLong(),
                any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByItem_OwnerIdAndEndIsAfter(anyLong(),
                any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByItem_OwnerIdAndStartBeforeAndEndAfter(anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, times(1)).findByItem_OwnerIdAndStatus(anyLong(),
                any(BookingStatus.class), any(Pageable.class));
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionWhenGetAllItemBookingsUserByStateIfUserNotExist() {
        BookingStatus bookingStatus = BookingStatus.REJECTED;
        long userId = 1L;
        int from = 0;
        int size = 10;

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                bookingService.getAllItemBookingsUser(userId, bookingStatus, from, size));

        assertEquals("Пользователя с id 1 не существует", exception.getMessage());

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByItem_OwnerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findByItem_OwnerIdAndEndIsBefore(anyLong(),
                any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByItem_OwnerIdAndEndIsAfter(anyLong(),
                any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByItem_OwnerIdAndStartBeforeAndEndAfter(anyLong(),
                any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class));
        verify(bookingRepository, never()).findByItem_OwnerIdAndStatus(anyLong(),
                any(BookingStatus.class), any(Pageable.class));
    }
}