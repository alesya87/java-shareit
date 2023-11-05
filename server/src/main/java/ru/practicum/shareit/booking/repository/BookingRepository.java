package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByItem_IdAndStartBeforeAndEndAfter(Long itemId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByBookerId(Long bookerId, Pageable pageable);

    List<Booking> findByEndIsBeforeAndBookerId(LocalDateTime localDateTime, Long bookerId, Pageable pageable);

    List<Booking> findByStartIsAfterAndBookerId(LocalDateTime localDateTime, Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime start,
                                                                          LocalDateTime end, Pageable pageable);

    List<Booking> findByStatusAndBookerId(BookingStatus status, Long bookerId, Pageable pageable);

    List<Booking> findByItem_OwnerId(Long ownerId, Pageable pageable);

    List<Booking> findByItem_OwnerIdAndEndIsBefore(Long ownerId, LocalDateTime localDateTime, Pageable pageable);

    List<Booking> findByItem_OwnerIdAndEndIsAfter(Long ownerId, LocalDateTime localDateTime, Pageable pageable);

    List<Booking> findByItem_OwnerIdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime start,
                                                                              LocalDateTime end, Pageable pageable);

    List<Booking> findByItem_OwnerIdAndStatus(Long ownerId, BookingStatus status, Pageable pageable);

    Booking findFirst1ByItemIdAndStartIsBeforeAndStatusNotOrderByStartDesc(Long itemId, LocalDateTime nowDateTime,
                                                                           BookingStatus bookingStatus);

    Booking findFirst1ByItemIdAndStartIsAfterAndStatusNotOrderByStart(Long itemId, LocalDateTime nowDateTime,
                                                                      BookingStatus bookingStatus);

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(Long userId, Long itemId,
                                                            BookingStatus bookingStatus, LocalDateTime localDateTime);

    List<Booking> findAllByItemIdInAndStatusNot(List<Long> itemIds, BookingStatus bookingStatus);
}
