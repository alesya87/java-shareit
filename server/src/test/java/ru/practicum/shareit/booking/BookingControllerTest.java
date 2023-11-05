package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingAddDto;
import ru.practicum.shareit.booking.dto.BookingLogDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemLogDto;
import ru.practicum.shareit.user.dto.UserLogDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    @InjectMocks
    private BookingController controller;
    @MockBean
    private BookingService bookingService;
    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldAddBooking() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        long bookingId = 1L;
        LocalDateTime start = LocalDateTime.of(2023, 10, 25, 22, 30);
        LocalDateTime end = LocalDateTime.of(2023, 10, 26, 22, 30);
        UserLogDto userLogDto = new UserLogDto(userId, "owner name", "owner@email.com");
        ItemLogDto itemLogDto = new ItemLogDto(itemId, "item name", "item description", true, userId,
                null, null, Collections.emptyList(), null);
        BookingAddDto bookingAddDto = new BookingAddDto(itemId, start, end);
        BookingLogDto bookingLogDto = new BookingLogDto(bookingId, itemLogDto, userLogDto, start, end, BookingStatus.PAST);

        when(bookingService.addBooking(userId, bookingAddDto)).thenReturn(bookingLogDto);

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingAddDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.start", containsString(start.toString())))
                .andExpect(jsonPath("$.end", containsString(end.toString())))
                .andExpect(jsonPath("$.booker.name", is(userLogDto.getName())))
                .andExpect(jsonPath("$.booker.email", is(userLogDto.getEmail())))
                .andExpect(jsonPath("$.item.name", is(itemLogDto.getName())))
                .andExpect(jsonPath("$.item.description", is(itemLogDto.getDescription())));

        verify(bookingService, times(1)).addBooking(userId, bookingAddDto);
    }

    @Test
    public void shouldUpdateBookingStatus() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        long bookingId = 1L;
        boolean approved = false;
        LocalDateTime start = LocalDateTime.of(2023, 10, 25, 22, 30);
        LocalDateTime end = LocalDateTime.of(2023, 10, 26, 22, 30);
        UserLogDto userLogDto = new UserLogDto(userId, "owner name", "owner@email.com");
        ItemLogDto itemLogDto = new ItemLogDto(itemId, "item name", "item description", true, userId,
                null, null, Collections.emptyList(), null);
        BookingLogDto bookingLogDto = new BookingLogDto(bookingId, itemLogDto, userLogDto, start, end, BookingStatus.PAST);

        when(bookingService.updateBookingStatus(userId, approved, bookingId)).thenReturn(bookingLogDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("approved", "false")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.start", containsString(start.toString())))
                .andExpect(jsonPath("$.end", containsString(end.toString())))
                .andExpect(jsonPath("$.booker.name", is(userLogDto.getName())))
                .andExpect(jsonPath("$.booker.email", is(userLogDto.getEmail())))
                .andExpect(jsonPath("$.item.name", is(itemLogDto.getName())))
                .andExpect(jsonPath("$.item.description", is(itemLogDto.getDescription())));

        verify(bookingService, times(1)).updateBookingStatus(userId, approved, bookingId);
    }

    @Test
    public void shouldThrowExceptionWhenUpdateBookingStatusIfApprovedNull() throws Exception {
        long bookingId = 1L;
        long itemId = 1L;
        long userId = 1L;
        LocalDateTime start = LocalDateTime.now().minusHours(5);
        LocalDateTime end = LocalDateTime.now().minusHours(1);
        BookingAddDto bookingAddDto = new BookingAddDto(itemId, start, end);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .content(mapper.writeValueAsString(bookingAddDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isInternalServerError());

        verify(bookingService, never()).updateBookingStatus(anyLong(), anyBoolean(), anyLong());
    }

    @Test
    public void shouldThrowExceptionWhenUpdateBookingStatusIfBookingIdNull() throws Exception {
        long itemId = 1L;
        long userId = 1L;
        LocalDateTime start = LocalDateTime.now().minusHours(5);
        LocalDateTime end = LocalDateTime.now().minusHours(1);
        BookingAddDto bookingAddDto = new BookingAddDto(itemId, start, end);

        mockMvc.perform(patch("/bookings")
                        .content(mapper.writeValueAsString(bookingAddDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("approved", "false")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isInternalServerError());

        verify(bookingService, never()).updateBookingStatus(anyLong(), anyBoolean(), anyLong());
    }

    @Test
    public void shouldReturnItemById() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        long bookingId = 1L;
        LocalDateTime start = LocalDateTime.of(2023, 10, 25, 22, 30);
        LocalDateTime end = LocalDateTime.of(2023, 10, 26, 22, 30);
        UserLogDto userLogDto = new UserLogDto(userId, "owner name", "owner@email.com");
        ItemLogDto itemLogDto = new ItemLogDto(itemId, "item name", "item description", true, userId,
                null, null, Collections.emptyList(), null);
        BookingLogDto bookingLogDto = new BookingLogDto(bookingId, itemLogDto, userLogDto, start, end, BookingStatus.PAST);

        when(bookingService.getBookingById(userId, bookingId)).thenReturn(bookingLogDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.start", containsString(start.toString())))
                .andExpect(jsonPath("$.end", containsString(end.toString())))
                .andExpect(jsonPath("$.booker.name", is(userLogDto.getName())))
                .andExpect(jsonPath("$.booker.email", is(userLogDto.getEmail())))
                .andExpect(jsonPath("$.item.name", is(itemLogDto.getName())))
                .andExpect(jsonPath("$.item.description", is(itemLogDto.getDescription())));

        verify(bookingService, times(1)).getBookingById(userId, bookingId);
    }

    @Test
    public void shouldReturnAllUserBookings() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        long bookingId = 1L;
        int from = 0;
        int size = 10;
        LocalDateTime startBooking1 = LocalDateTime.of(2023, 10, 25, 22, 30);
        LocalDateTime endBooking1 = LocalDateTime.of(2023, 10, 26, 22, 30);
        LocalDateTime startBooking2 = LocalDateTime.of(2023, 1, 25, 22, 30);
        LocalDateTime endBooking2 = LocalDateTime.of(2023, 1, 26, 22, 30);
        UserLogDto userLogDto = new UserLogDto(userId, "owner name", "owner@email.com");
        ItemLogDto itemLogDto1 = new ItemLogDto(itemId, "item1 name", "item1 description", true, userId,
                null, null, Collections.emptyList(), null);
        ItemLogDto itemLogDto2 = new ItemLogDto(itemId, "item2 name", "item2 description", true, userId,
                null, null, Collections.emptyList(), null);
        BookingLogDto bookingLogDto1 = new BookingLogDto(bookingId, itemLogDto1, userLogDto,
                startBooking1, endBooking1, BookingStatus.PAST);
        BookingLogDto bookingLogDto2 = new BookingLogDto(2L, itemLogDto2, userLogDto,
                startBooking2, endBooking2, BookingStatus.PAST);
        List<BookingLogDto> bookingLogDtos = List.of(bookingLogDto1, bookingLogDto2);

        when(bookingService.getAllUserBookings(BookingStatus.ALL, userId, from, size)).thenReturn(bookingLogDtos);

        mockMvc.perform(get("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].start", containsString(startBooking1.toString())))
                .andExpect(jsonPath("$[0].end", containsString(endBooking1.toString())))
                .andExpect(jsonPath("$[0].booker.name", is(bookingLogDto1.getBooker().getName())))
                .andExpect(jsonPath("$[0].booker.email", is(bookingLogDto1.getBooker().getEmail())))
                .andExpect(jsonPath("$[0].item.name", is(itemLogDto1.getName())))
                .andExpect(jsonPath("$[0].item.description", is(itemLogDto1.getDescription())))
                .andExpect(jsonPath("$[1].start", containsString(startBooking2.toString())))
                .andExpect(jsonPath("$[1].end", containsString(endBooking2.toString())))
                .andExpect(jsonPath("$[1].booker.name", is(bookingLogDto2.getBooker().getName())))
                .andExpect(jsonPath("$[1].booker.email", is(bookingLogDto2.getBooker().getEmail())))
                .andExpect(jsonPath("$[1].item.name", is(itemLogDto2.getName())))
                .andExpect(jsonPath("$[1].item.description", is(itemLogDto2.getDescription())));

        verify(bookingService, times(1)).getAllUserBookings(BookingStatus.ALL, userId, from, size);
    }

    @Test
    public void shouldReturnAllItemBookingsUser() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        long bookingId = 1L;
        int from = 0;
        int size = 10;
        LocalDateTime startBooking1 = LocalDateTime.of(2023, 10, 25, 22, 30);
        LocalDateTime endBooking1 = LocalDateTime.of(2023, 10, 26, 22, 30);
        LocalDateTime startBooking2 = LocalDateTime.of(2023, 1, 25, 22, 30);
        LocalDateTime endBooking2 = LocalDateTime.of(2023, 1, 26, 22, 30);
        UserLogDto userLogDto = new UserLogDto(userId, "owner name", "owner@email.com");
        ItemLogDto itemLogDto1 = new ItemLogDto(itemId, "item1 name", "item1 description", true, userId,
                null, null, Collections.emptyList(), null);
        ItemLogDto itemLogDto2 = new ItemLogDto(itemId, "item2 name", "item2 description", true, userId,
                null, null, Collections.emptyList(), null);
        BookingLogDto bookingLogDto1 = new BookingLogDto(bookingId, itemLogDto1, userLogDto,
                startBooking1, endBooking1, BookingStatus.PAST);
        BookingLogDto bookingLogDto2 = new BookingLogDto(2L, itemLogDto2, userLogDto,
                startBooking2, endBooking2, BookingStatus.PAST);
        List<BookingLogDto> bookingLogDtos = List.of(bookingLogDto1, bookingLogDto2);

        when(bookingService.getAllItemBookingsUser(userId, BookingStatus.ALL, from, size)).thenReturn(bookingLogDtos);

        mockMvc.perform(get("/bookings/owner")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].start", containsString(startBooking1.toString())))
                .andExpect(jsonPath("$[0].end", containsString(endBooking1.toString())))
                .andExpect(jsonPath("$[0].booker.name", is(bookingLogDto1.getBooker().getName())))
                .andExpect(jsonPath("$[0].booker.email", is(bookingLogDto1.getBooker().getEmail())))
                .andExpect(jsonPath("$[0].item.name", is(itemLogDto1.getName())))
                .andExpect(jsonPath("$[0].item.description", is(itemLogDto1.getDescription())))
                .andExpect(jsonPath("$[1].start", containsString(startBooking2.toString())))
                .andExpect(jsonPath("$[1].end", containsString(endBooking2.toString())))
                .andExpect(jsonPath("$[1].booker.name", is(bookingLogDto2.getBooker().getName())))
                .andExpect(jsonPath("$[1].booker.email", is(bookingLogDto2.getBooker().getEmail())))
                .andExpect(jsonPath("$[1].item.name", is(itemLogDto2.getName())))
                .andExpect(jsonPath("$[1].item.description", is(itemLogDto2.getDescription())));

        verify(bookingService, times(1)).getAllItemBookingsUser(userId, BookingStatus.ALL, from, size);
    }
}
