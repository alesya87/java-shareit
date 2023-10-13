package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByItem_IdAndStartBeforeAndEndAfter(Long itemId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByEndIsBeforeAndBookerIdOrderByStartDesc(LocalDateTime localDateTime, Long bookerId);

    List<Booking> findByStartIsAfterAndBookerIdOrderByStartDesc(LocalDateTime localDateTime, Long bookerId);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long ownerId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByStatusAndBookerIdOrderByStartDesc(BookingStatus status, Long bookerId);

    List<Booking> findByItem_OwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findByItem_OwnerIdAndEndIsBeforeOrderByStartDesc(Long ownerId, LocalDateTime localDateTime);

    List<Booking> findByItem_OwnerIdAndEndIsAfterOrderByStartDesc(Long ownerId, LocalDateTime localDateTime);

    List<Booking> findByItem_OwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long ownerId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByItem_OwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status);

    Booking findFirst1ByItemIdAndStartIsBeforeAndStatusNotOrderByStart(Long itemId, LocalDateTime nowDateTime, BookingStatus bookingStatus);

    Booking findFirst1ByItemIdAndStartIsAfterAndStatusNotOrderByStart(Long itemId, LocalDateTime nowDateTime, BookingStatus bookingStatus);
}
