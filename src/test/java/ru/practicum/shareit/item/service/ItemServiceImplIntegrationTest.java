package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingAddDto;
import ru.practicum.shareit.booking.dto.BookingLogDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.comment.dto.CommentAddDto;
import ru.practicum.shareit.item.comment.dto.CommentInItemLogDto;
import ru.practicum.shareit.item.dto.ItemAddDto;
import ru.practicum.shareit.item.dto.ItemLogDto;
import ru.practicum.shareit.request.dto.ItemRequestAddDto;
import ru.practicum.shareit.request.dto.ItemRequestLogDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserAddDto;
import ru.practicum.shareit.user.dto.UserLogDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemServiceImplIntegrationTest {
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ItemRequestService itemRequestService;

    @Test
    public void integrationItemTest() throws InterruptedException {
        UserAddDto userAddDto1 = new UserAddDto("user1 name", "user1@email.com");
        UserLogDto userLogDto1 = userService.addUser(userAddDto1);
        UserAddDto userAddDto2 = new UserAddDto("user2 name", "user2@email.com");
        UserLogDto userLogDto2 = userService.addUser(userAddDto2);

        ItemRequestAddDto itemRequestAddDto = new ItemRequestAddDto("Швейная машинка");
        ItemRequestLogDto itemRequestLogDto = itemRequestService.addItemRequest(itemRequestAddDto, userLogDto1.getId());
        ItemAddDto itemAddDto1 = new ItemAddDto("Швейная машинка", "ZINGER",
                true, itemRequestLogDto.getId());
        ItemLogDto itemLogDto1 = itemService.addItem(itemAddDto1, userLogDto2.getId());
        ItemLogDto itemLogDto1WithRequest = itemService.getItemById(itemLogDto1.getId(), userLogDto2.getId());
        assertEquals(itemLogDto1, itemLogDto1WithRequest);
        assertEquals(itemLogDto1.getRequestId(), itemLogDto1WithRequest.getRequestId());
        assertEquals(1, itemService.getAllItemsByOwnerId(userLogDto2.getId(), 0, 10).size());

        ItemAddDto itemAddDto2 = new ItemAddDto("Книга по психологии", "Счастлив по собственному желанию",
                true, null);
        ItemLogDto itemLogDto2 = itemService.addItem(itemAddDto2, userLogDto2.getId());
        LocalDateTime startBooking = LocalDateTime.now().plusSeconds(1);
        LocalDateTime endBooking = LocalDateTime.now().plusSeconds(2);
        BookingAddDto bookingAddDto = new BookingAddDto(itemLogDto2.getId(), startBooking, endBooking);
        BookingLogDto bookingLogDto = bookingService.addBooking(userLogDto1.getId(), bookingAddDto);
        bookingService.updateBookingStatus(userLogDto2.getId(), true, bookingLogDto.getId());
        BookingLogDto bookingLogDtoUpdated = bookingService.getBookingById(userLogDto1.getId(), bookingLogDto.getId());
        Thread.sleep(2000);
        CommentAddDto commentAddDto = new CommentAddDto("Хорошая вещь");
        CommentInItemLogDto commentInItemLogDto = itemService.addComment(commentAddDto, userLogDto1.getId(), itemLogDto2.getId());
        ItemLogDto itemLogDto2WithBookingAndComment = itemService.getItemById(itemLogDto2.getId(), userLogDto2.getId());
        assertEquals(bookingLogDtoUpdated.getId(), itemLogDto2WithBookingAndComment.getLastBooking().getId());
        assertEquals(bookingLogDtoUpdated.getStart(), itemLogDto2WithBookingAndComment.getLastBooking().getStart());
        assertEquals(bookingLogDtoUpdated.getEnd(), itemLogDto2WithBookingAndComment.getLastBooking().getEnd());
        assertEquals(commentInItemLogDto.getText(), itemLogDto2WithBookingAndComment.getComments().get(0).getText());
        assertEquals(commentInItemLogDto.getAuthorName(), itemLogDto2WithBookingAndComment.getComments().get(0).getAuthorName());
        assertEquals(commentInItemLogDto.getCreated(), itemLogDto2WithBookingAndComment.getComments().get(0).getCreated());
        assertEquals(2, itemService.getAllItemsByOwnerId(userLogDto2.getId(), 0, 10).size());

        ItemAddDto itemAddDto3 = new ItemAddDto("Синтезатор", "CASIO",
                true, null);
        ItemLogDto itemLogDto3 = itemService.addItem(itemAddDto3, userLogDto1.getId());
        assertEquals(itemLogDto3, itemService.getItemById(itemLogDto3.getId(), userLogDto1.getId()));
        assertEquals(Collections.emptyList(), itemService.getItemsBySearchQuery("что-то", 0, 10));
        assertEquals(1, itemService.getAllItemsByOwnerId(userLogDto1.getId(),0, 10).size());
        itemService.deleteItemById(itemLogDto3.getId());
        assertEquals(0, itemService.getAllItemsByOwnerId(userLogDto1.getId(),0, 10).size());
    }
}