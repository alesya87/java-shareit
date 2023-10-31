package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingAddDto;
import ru.practicum.shareit.booking.dto.BookingLogDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemAddDto;
import ru.practicum.shareit.item.dto.ItemLogDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserAddDto;
import ru.practicum.shareit.user.dto.UserLogDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BookingServiceImplIntegrationTest {
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private BookingService bookingService;

    @Test
    public void integrationItemTest() throws InterruptedException {
        UserAddDto userAddDto1 = new UserAddDto("user1 name", "user1@email.com");
        UserLogDto userLogDto1 = userService.addUser(userAddDto1);
        UserAddDto userAddDto2 = new UserAddDto("user2 name", "user2@email.com");
        UserLogDto userLogDto2 = userService.addUser(userAddDto2);

        ItemAddDto itemAddDto1 = new ItemAddDto("Швейная машинка", "ZINGER",
                true, null);
        ItemLogDto itemLogDto1 = itemService.addItem(itemAddDto1, userLogDto1.getId());
        ItemAddDto itemAddDto2 = new ItemAddDto("Книга по психологии", "Счастлив по собственному желанию",
                true, null);
        ItemLogDto itemLogDto2 = itemService.addItem(itemAddDto2, userLogDto2.getId());

        LocalDateTime startBooking1 = LocalDateTime.now().plusSeconds(1);
        LocalDateTime endBooking1 = LocalDateTime.now().plusSeconds(2);
        BookingAddDto bookingAddDto1 = new BookingAddDto(itemLogDto2.getId(), startBooking1, endBooking1);
        BookingLogDto bookingLogDto1 = bookingService.addBooking(userLogDto1.getId(), bookingAddDto1);
        bookingService.updateBookingStatus(userLogDto2.getId(), true, bookingLogDto1.getId());
        BookingLogDto bookingLogDtoApproved = bookingService.getBookingById(userLogDto1.getId(), bookingLogDto1.getId());
        Thread.sleep(2000);
        assertEquals(BookingStatus.APPROVED, bookingLogDtoApproved.getStatus());
        assertEquals(bookingLogDtoApproved,
                bookingService.getAllUserBookings(BookingStatus.PAST, userLogDto1.getId(), 0, 10).get(0));
        assertEquals(bookingLogDtoApproved,
                bookingService.getAllItemBookingsUser(userLogDto2.getId(), BookingStatus.ALL, 0, 10).get(0));
        assertEquals(1, bookingService.getAllUserBookings(BookingStatus.PAST, userLogDto1.getId(), 0, 10).size());
        assertEquals(1, bookingService.getAllItemBookingsUser(userLogDto2.getId(), BookingStatus.ALL, 0, 10).size());
        assertEquals(itemLogDto2.getName(), bookingLogDtoApproved.getItem().getName());
        assertEquals(itemLogDto2.getDescription(), bookingLogDtoApproved.getItem().getDescription());
        assertEquals(itemLogDto2.getAvailable(), bookingLogDtoApproved.getItem().getAvailable());
        assertEquals(userLogDto1.getName(), bookingLogDtoApproved.getBooker().getName());
        assertEquals(userLogDto1.getEmail(), bookingLogDtoApproved.getBooker().getEmail());

        LocalDateTime startBooking2 = LocalDateTime.now().plusSeconds(1);
        LocalDateTime endBooking2 = LocalDateTime.now().plusSeconds(2);
        BookingAddDto bookingAddDto2 = new BookingAddDto(itemLogDto1.getId(), startBooking2, endBooking2);
        BookingLogDto bookingLogDto2 = bookingService.addBooking(userLogDto2.getId(), bookingAddDto2);
        bookingService.updateBookingStatus(userLogDto1.getId(), false, bookingLogDto2.getId());
        BookingLogDto bookingLogDtoRejected = bookingService.getBookingById(userLogDto2.getId(), bookingLogDto2.getId());
        Thread.sleep(2000);
        assertEquals(BookingStatus.REJECTED, bookingLogDtoRejected.getStatus());
        assertEquals(bookingLogDtoRejected, bookingService.getAllUserBookings(BookingStatus.REJECTED, userLogDto2.getId(), 0, 10).get(0));
        assertEquals(bookingLogDtoRejected, bookingService.getAllItemBookingsUser(userLogDto1.getId(), BookingStatus.ALL, 0, 10).get(0));
        assertEquals(itemLogDto1.getName(), bookingLogDtoRejected.getItem().getName());
        assertEquals(itemLogDto1.getDescription(), bookingLogDtoRejected.getItem().getDescription());
        assertEquals(itemLogDto1.getAvailable(), bookingLogDtoRejected.getItem().getAvailable());
        assertEquals(userLogDto2.getName(), bookingLogDtoRejected.getBooker().getName());
        assertEquals(userLogDto2.getEmail(), bookingLogDtoRejected.getBooker().getEmail());
    }
}