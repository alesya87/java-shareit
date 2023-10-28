package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        LocalDateTime start = LocalDateTime.now().minusHours(5);
        LocalDateTime end = LocalDateTime.now().minusHours(1);
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
                .andExpect(jsonPath("$.start", is(start.toString())))
                .andExpect(jsonPath("$.end", is(end.toString())))
                .andExpect(jsonPath("$.booker.name", is(userLogDto.getName())))
                .andExpect(jsonPath("$.booker.email", is(userLogDto.getEmail())))
                .andExpect(jsonPath("$.item.name", is(itemLogDto.getName())))
                .andExpect(jsonPath("$.item.description", is(itemLogDto.getDescription())));

        verify(bookingService, times(1)).addBooking(userId, bookingAddDto);
    }

    @Test
    public void shouldThrowExceptionWhenAddBookingIfNoUserHeader() throws Exception {
        long itemId = 1L;
        LocalDateTime start = LocalDateTime.now().minusHours(5);
        LocalDateTime end = LocalDateTime.now().minusHours(1);
        BookingAddDto bookingAddDto = new BookingAddDto(itemId, start, end);
        Matcher<String> contentMatcher = CoreMatchers
                .containsString("'X-Sharer-User-Id' for method parameter type Long is not present");

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingAddDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(contentMatcher));

        verify(bookingService, never()).addBooking(anyLong(), any(BookingAddDto.class));
    }

    @Test
    public void shouldThrowExceptionWhenAddBookingIfItemIdNull() throws Exception {
        long userId = 1L;
        LocalDateTime start = LocalDateTime.now().minusHours(5);
        LocalDateTime end = LocalDateTime.now().minusHours(1);
        BookingAddDto bookingAddDto = new BookingAddDto(null, start, end);
        Matcher<String> contentMatcher = CoreMatchers
                .containsString("default message [itemId]]; default message [must not be null]]");

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingAddDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(contentMatcher));

        verify(bookingService, never()).addBooking(anyLong(), any(BookingAddDto.class));
    }

    @Test
    public void shouldThrowExceptionWhenAddBookingIfStartNull() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        LocalDateTime end = LocalDateTime.now().minusHours(1);
        BookingAddDto bookingAddDto = new BookingAddDto(itemId, null, end);
        Matcher<String> contentMatcher = CoreMatchers
                .containsString("default message [start]]; default message [must not be null]]");

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingAddDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(contentMatcher));

        verify(bookingService, never()).addBooking(anyLong(), any(BookingAddDto.class));
    }

    @Test
    public void shouldThrowExceptionWhenAddBookingIfEndNull() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        LocalDateTime start = LocalDateTime.now().minusHours(5);
        BookingAddDto bookingAddDto = new BookingAddDto(itemId, start, null);
        Matcher<String> contentMatcher = CoreMatchers
                .containsString("default message [end]]; default message [must not be null]]");

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingAddDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(contentMatcher));

        verify(bookingService, never()).addBooking(anyLong(), any(BookingAddDto.class));
    }

    @Test
    public void shouldUpdateBookingStatus() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        long bookingId = 1L;
        boolean approved = false;
        LocalDateTime start = LocalDateTime.now().minusHours(5);
        LocalDateTime end = LocalDateTime.now().minusHours(1);
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
    public void shouldThrowExceptionWhenUpdateBookingStatusIfNoUserHeader() throws Exception {
        long bookingId = 1L;
        long itemId = 1L;
        LocalDateTime start = LocalDateTime.now().minusHours(5);
        LocalDateTime end = LocalDateTime.now().minusHours(1);
        BookingAddDto bookingAddDto = new BookingAddDto(itemId, start, end);
        Matcher<String> contentMatcher = CoreMatchers
                .containsString("'X-Sharer-User-Id' for method parameter type Long is not present");

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .content(mapper.writeValueAsString(bookingAddDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("approved", "false")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(contentMatcher));

        verify(bookingService, never()).updateBookingStatus(anyLong(), anyBoolean(), anyLong());
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
        LocalDateTime start = LocalDateTime.now().minusHours(5);
        LocalDateTime end = LocalDateTime.now().minusHours(1);
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
                .andExpect(jsonPath("$.start", is(start.toString())))
                .andExpect(jsonPath("$.end", is(end.toString())))
                .andExpect(jsonPath("$.booker.name", is(userLogDto.getName())))
                .andExpect(jsonPath("$.booker.email", is(userLogDto.getEmail())))
                .andExpect(jsonPath("$.item.name", is(itemLogDto.getName())))
                .andExpect(jsonPath("$.item.description", is(itemLogDto.getDescription())));

        verify(bookingService, times(1)).getBookingById(userId, bookingId);
    }

    @Test
    public void shouldThrowExceptionWhenGetBookingByIdIfNoUserHeader() throws Exception {
        long bookingId = 1L;
        Matcher<String> contentMatcher = CoreMatchers
                .containsString("'X-Sharer-User-Id' for method parameter type Long is not present");

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(contentMatcher));

        verify(bookingService, never()).getBookingById(anyLong(), anyLong());
    }

    @Test
    public void shouldReturnAllUserBookings() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        long bookingId = 1L;
        int from = 0;
        int size = 10;
        LocalDateTime startBooking1 = LocalDateTime.now().minusDays(3);
        LocalDateTime endBooking1 = LocalDateTime.now().minusDays(1);
        LocalDateTime startBooking2 = LocalDateTime.now().minusHours(9);
        LocalDateTime endBooking2 = LocalDateTime.now().minusHours(4);
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
    public void shouldThrowExceptionWhenGetAllUserBookingsIfFromNegative() throws Exception {
        long userId = 1L;
        Matcher<String> contentMatcher = CoreMatchers
                .containsString("from: must be greater than or equal to 0");

        mockMvc.perform(get("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "ALL")
                        .param("from", "-1")
                        .param("size", "10")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(contentMatcher));

        verify(bookingService, never()).getAllUserBookings(any(BookingStatus.class), anyLong(), anyInt(), anyInt());
    }

    @Test
    public void shouldThrowExceptionWhenGetAllUserBookingsIfSize0() throws Exception {
        long userId = 1L;
        Matcher<String> contentMatcher = CoreMatchers
                .containsString("size: must be greater than or equal to 1");

        mockMvc.perform(get("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "0")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(contentMatcher));

        verify(bookingService, never()).getAllUserBookings(any(BookingStatus.class), anyLong(), anyInt(), anyInt());
    }

    @Test
    public void shouldThrowExceptionWhenGetAllUserBookingsIfNoUserHeader() throws Exception {
        Matcher<String> contentMatcher = CoreMatchers
                .containsString("'X-Sharer-User-Id' for method parameter type Long is not present");

        mockMvc.perform(get("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(contentMatcher));

        verify(bookingService, never()).getAllUserBookings(any(BookingStatus.class), anyLong(), anyInt(), anyInt());
    }

    @Test
    public void shouldReturnAllItemBookingsUser() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        long bookingId = 1L;
        int from = 0;
        int size = 10;
        LocalDateTime startBooking1 = LocalDateTime.now().minusDays(3);
        LocalDateTime endBooking1 = LocalDateTime.now().minusDays(1);
        LocalDateTime startBooking2 = LocalDateTime.now().minusHours(9);
        LocalDateTime endBooking2 = LocalDateTime.now().minusHours(4);
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

    @Test
    public void shouldThrowExceptionWhenGetAllItemBookingsUserIfFromNegative() throws Exception {
        long userId = 1L;
        Matcher<String> contentMatcher = CoreMatchers
                .containsString("from: must be greater than or equal to 0");

        mockMvc.perform(get("/bookings/owner")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "ALL")
                        .param("from", "-1")
                        .param("size", "10")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(contentMatcher));

        verify(bookingService, never()).getAllItemBookingsUser(anyLong(), any(BookingStatus.class), anyInt(), anyInt());
    }

    @Test
    public void shouldThrowExceptionWhenGetAllItemBookingsUserIfSize0() throws Exception {
        long userId = 1L;
        Matcher<String> contentMatcher = CoreMatchers
                .containsString("size: must be greater than or equal to 1");

        mockMvc.perform(get("/bookings/owner")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "0")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(contentMatcher));

        verify(bookingService, never()).getAllItemBookingsUser(anyLong(), any(BookingStatus.class), anyInt(), anyInt());
    }

    @Test
    public void shouldThrowExceptionWhenGetAllItemBookingsUserIfNoUserHeader() throws Exception {
        Matcher<String> contentMatcher = CoreMatchers
                .containsString("'X-Sharer-User-Id' for method parameter type Long is not present");

        mockMvc.perform(get("/bookings/owner")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(contentMatcher));

        verify(bookingService, never()).getAllItemBookingsUser(anyLong(), any(BookingStatus.class), anyInt(), anyInt());
    }
}
