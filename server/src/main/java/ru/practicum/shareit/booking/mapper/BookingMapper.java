package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingAddDto;
import ru.practicum.shareit.booking.dto.BookingLogDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class BookingMapper {
    public static BookingLogDto mapToBookingLogDto(Booking booking) {
        return BookingLogDto.builder()
                .id(booking.getId())
                .item(ItemMapper.mapToItemLogDto(booking.getItem()))
                .booker(UserMapper.mapToUserLogDto(booking.getBooker()))
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .build();
    }

    public static List<BookingLogDto> mapToListBookingDto(List<Booking> bookings) {
        return bookings.stream()
                .map(BookingMapper::mapToBookingLogDto)
                .collect(Collectors.toList());
    }

    public static BookingShortDto mapToBookingShortDto(Booking booking) {
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }

    public static Booking mapToBooking(BookingAddDto bookingAddDto, User booker, Item item) {
        return Booking.builder()
                .start(bookingAddDto.getStart())
                .end(bookingAddDto.getEnd())
                .booker(booker)
                .item(item)
                .status(BookingStatus.WAITING)
                .build();
    }
}
