package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.EntityNotAvailableException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.comment.dto.CommentAddDto;
import ru.practicum.shareit.item.comment.dto.CommentInItemLogDto;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemAddDto;
import ru.practicum.shareit.item.dto.ItemLogDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
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
class ItemServiceImplTest {
    private ItemService itemService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @BeforeEach
    public void setUp() {
        itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    public void shouldAddItem() {
        long ownerId = 1L;
        User owner = new User(ownerId, "user name", "user@email.com");
        ItemRequest itemRequest = new ItemRequest(1L, "itemRequest description", owner,
                LocalDateTime.now(), Collections.emptyList());
        ItemAddDto itemAddDto = new ItemAddDto("item name", "item description", true, itemRequest.getId());
        Item item = new Item(1L, "item name", "item description", true, owner,
                null, null, Collections.emptyList(), itemRequest);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(itemRequest.getId())).thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemLogDto itemLogDto = itemService.addItem(itemAddDto, ownerId);

        assertNotNull(itemLogDto);

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRequestRepository, times(1)).findById(itemRequest.getId());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionWhenAddItemWithOwnerNotExist() {
        long ownerId = 1L;
        ItemAddDto itemAddDto = new ItemAddDto("item name", "item description", true, null);

        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () -> itemService.addItem(itemAddDto, ownerId));

        verify(itemRequestRepository, never()).findById(anyLong());
        verify(itemRepository, never()).save(any(Item.class));

        assertEquals("Владелец с id 1 не найден", exception.getMessage());
    }

    @Test
    public void shouldAddItemIfRequestIdIsNull() {
        long ownerId = 1L;
        User owner = new User(ownerId, "user name", "user@email.com");
        ItemAddDto itemAddDto = new ItemAddDto("item name", "item description", true, null);
        Item item = new Item(1L, "item name", "item description", true, owner,
                null, null, Collections.emptyList(), null);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemLogDto itemLogDto = itemService.addItem(itemAddDto, ownerId);

        assertNotNull(itemLogDto);

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRequestRepository, never()).findById(anyLong());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    public void shouldUpdateItem() {
        long ownerId = 1L;
        long itemId = 1L;
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto(itemId, "itemUpdateDto name",
                "itemUpdateDto description", false, ownerId);
        User owner = new User(ownerId, "user name", "user@email.com");
        Item itemBeforeUpdate = new Item(itemId, "item name", "item description",
                true, owner, null, null, Collections.emptyList(), null);
        ItemLogDto expectedItemLogDto = new ItemLogDto(itemId, "itemUpdateDto name",
                "itemUpdateDto description", false, ownerId, null,
                null, Collections.emptyList(), null);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(itemBeforeUpdate));
        when(itemRepository.save(any(Item.class))).thenReturn(itemBeforeUpdate);

        ItemLogDto result = itemService.updateItem(itemUpdateDto, itemId, ownerId);

        assertEquals(expectedItemLogDto, result);

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionWhenUpdateIfItemNotExist() {
        long ownerId = 1L;
        long itemId = 1L;
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto(itemId, "itemUpdateDto name",
                "itemUpdateDto description", false, ownerId);

        when(itemRepository.findById(ownerId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                itemService.updateItem(itemUpdateDto, itemId, ownerId));

        assertEquals("Item с id 1 не найден", exception.getMessage());

        verify(itemRepository, times(1)).findById(itemId);
        verify(userRepository, never()).findById(anyLong());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionWhenUpdateIfOwnerNotExist() {
        long ownerId = 1L;
        long itemId = 1L;
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto(itemId, "itemUpdateDto name",
                "itemUpdateDto description", false, ownerId);
        User owner = new User(ownerId, "user name", "user@email.com");
        Item item = new Item(itemId, "item name", "item description", true, owner,
                null, null, Collections.emptyList(), null);

        when(itemRepository.findById(ownerId)).thenReturn(Optional.of(item));
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                itemService.updateItem(itemUpdateDto, itemId, ownerId));

        assertEquals("Владелец с id 1 не найден", exception.getMessage());

        verify(itemRepository, times(1)).findById(itemId);
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionWhenUpdateIfOwnerIsWrong() {
        long wrongOwnerId = 2L;
        long ownerId = 1L;
        long itemId = 1L;
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto(itemId, "itemUpdateDto name",
                "itemUpdateDto description", false, wrongOwnerId);
        User owner = new User(ownerId, "user name", "user@email.com");
        User wrongOwner = new User(wrongOwnerId, "wrongOwner name", "wrongOwner@email.com");
        Item itemBeforeUpdate = new Item(itemId, "item name", "item description", true, owner,
                null, null, Collections.emptyList(), null);

        when(itemRepository.findById(ownerId)).thenReturn(Optional.of(itemBeforeUpdate));
        when(userRepository.findById(wrongOwnerId)).thenReturn(Optional.of(wrongOwner));

        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                itemService.updateItem(itemUpdateDto, itemId, wrongOwnerId));

        assertEquals("Владелец c id 2 у item с id 1 не найден", exception.getMessage());

        verify(itemRepository, times(1)).findById(itemId);
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    public void shouldUpdateItemName() {
        long ownerId = 1L;
        long itemId = 1L;
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto(itemId, "itemUpdateDto name", null, null, ownerId);
        User owner = new User(ownerId, "user name", "user@email.com");
        Item item = new Item(itemId, "item name", "item description", true, owner,
                null, null, Collections.emptyList(), null);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        assertEquals("item name", item.getName());

        itemService.updateItem(itemUpdateDto, itemId, ownerId);

        assertEquals("itemUpdateDto name", item.getName());
        assertEquals("item description", item.getDescription());
        assertEquals(true, item.getAvailable());

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    public void shouldUpdateItemDescription() {
        long ownerId = 1L;
        long itemId = 1L;
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto(itemId, null, "itemUpdateDto description",
                null, ownerId);
        User owner = new User(ownerId, "user name", "user@email.com");
        Item item = new Item(itemId, "item name", "item description", true, owner,
                null, null, Collections.emptyList(), null);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        assertEquals("item description", item.getDescription());

        itemService.updateItem(itemUpdateDto, itemId, ownerId);

        assertEquals("item name", item.getName());
        assertEquals("itemUpdateDto description", item.getDescription());
        assertEquals(true, item.getAvailable());

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    public void shouldUpdateItemAvailable() {
        long ownerId = 1L;
        long itemId = 1L;
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto(itemId, null, null, false, ownerId);
        User owner = new User(ownerId, "user name", "user@email.com");
        Item item = new Item(itemId, "item name", "item description", true, owner,
                null, null, Collections.emptyList(), null);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        assertEquals(true, item.getAvailable());

        itemService.updateItem(itemUpdateDto, itemId, ownerId);

        assertEquals("item name", item.getName());
        assertEquals("item description", item.getDescription());
        assertEquals(false, item.getAvailable());

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    public void shouldReturnItemWhenGetByIdForOwnerWithBooking() {
        long ownerId = 1L;
        long itemId = 1L;
        LocalDateTime startLastBooking = LocalDateTime.now().minusDays(3);
        LocalDateTime endLastBooking = LocalDateTime.now().minusDays(2);
        LocalDateTime startNextBooking = LocalDateTime.now().plusDays(1);
        LocalDateTime endNextBooking = LocalDateTime.now().plusDays(2);
        User owner = new User(ownerId, "user name", "user@email.com");
        Item item = new Item(itemId, "item name", "item description", true, owner,
                null, null, Collections.emptyList(), null);
        Booking lastBooking = new Booking(1L, startLastBooking, endLastBooking, item, owner, BookingStatus.PAST);
        Booking nextBooking = new Booking(2L, startNextBooking, endNextBooking, item, owner, BookingStatus.PAST);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findFirst1ByItemIdAndStartIsBeforeAndStatusNotOrderByStartDesc(anyLong(),
                any(LocalDateTime.class), any(BookingStatus.class))).thenReturn(lastBooking);
        when(bookingRepository.findFirst1ByItemIdAndStartIsAfterAndStatusNotOrderByStart(anyLong(),
                any(LocalDateTime.class), any(BookingStatus.class))).thenReturn(nextBooking);

        ItemLogDto result = itemService.getItemById(itemId, ownerId);

        assertNotNull(result.getLastBooking());
        assertNotNull(result.getNextBooking());
        assertEquals(ItemMapper.mapToItemLogDto(item), result);

        verify(itemRepository, times(1)).findById(itemId);
        verify(bookingRepository, times(1))
                .findFirst1ByItemIdAndStartIsBeforeAndStatusNotOrderByStartDesc(anyLong(),
                        any(LocalDateTime.class), any(BookingStatus.class));
        verify(bookingRepository, times(1))
                .findFirst1ByItemIdAndStartIsAfterAndStatusNotOrderByStart(anyLong(),
                        any(LocalDateTime.class), any(BookingStatus.class));
    }

    @Test
    public void shouldReturnItemWhenGetByIdForOtherUserWithoutBooking() {
        long ownerId = 1L;
        long itemId = 1L;
        long wrongOwnerId = 2L;
        User owner = new User(ownerId, "user name", "user@email.com");
        Item item = new Item(itemId, "item name", "item description", true, owner, null, null,
                Collections.emptyList(), null);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        ItemLogDto result = itemService.getItemById(itemId, wrongOwnerId);

        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertEquals(ItemMapper.mapToItemLogDto(item), result);

        verify(itemRepository, times(1)).findById(itemId);
        verify(bookingRepository, never()).findFirst1ByItemIdAndStartIsBeforeAndStatusNotOrderByStartDesc(anyLong(),
                any(LocalDateTime.class), any(BookingStatus.class));
        verify(bookingRepository, never()).findFirst1ByItemIdAndStartIsAfterAndStatusNotOrderByStart(anyLong(),
                any(LocalDateTime.class), any(BookingStatus.class));
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionWhenGetByIdIfItemNotExist() {
        long ownerId = 1L;
        long itemId = 1L;

        when(itemRepository.findById(ownerId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () -> itemService.getItemById(itemId, ownerId));

        assertEquals("item с id 1 не найден", exception.getMessage());

        verify(itemRepository, times(1)).findById(itemId);
        verify(bookingRepository, never()).findFirst1ByItemIdAndStartIsBeforeAndStatusNotOrderByStartDesc(anyLong(),
                any(LocalDateTime.class), any(BookingStatus.class));
        verify(bookingRepository, never()).findFirst1ByItemIdAndStartIsAfterAndStatusNotOrderByStart(anyLong(),
                any(LocalDateTime.class), any(BookingStatus.class));
    }

    @Test
    public void shouldReturnAllItemsByOwnerId() {
        long ownerId = 1L;
        int from = 0;
        int size = 10;
        Sort sort = Sort.by(Sort.Order.asc("id"));
        Pageable pageable = PageRequest.of(0, size, sort);
        User targetOwner = new User(ownerId, "user name", "user@email.com");
        Item item1 = new Item(1L, "item1 name", "item1 description",
                true, targetOwner, null, null, Collections.emptyList(), null);
        Item item2 = new Item(3L, "item3 name", "item3 description",
                true, targetOwner, null, null, Collections.emptyList(), null);
        List<Item> items = List.of(item1, item2);

        LocalDateTime startBooking1 = LocalDateTime.now().minusDays(3);
        LocalDateTime endBooking1 = LocalDateTime.now().minusDays(2);
        LocalDateTime startBooking2 = LocalDateTime.now().plusDays(1);
        LocalDateTime endBooking2 = LocalDateTime.now().plusDays(2);
        Booking booking1 = new Booking(1L, startBooking1, endBooking1, item1, targetOwner, BookingStatus.PAST);
        Booking booking2 = new Booking(3L, startBooking2, endBooking2, item1, targetOwner, BookingStatus.FUTURE);
        List<Booking> bookings = List.of(booking1, booking2);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(targetOwner));
        when(itemRepository.findByOwnerId(ownerId, pageable)).thenReturn(items);
        when(bookingRepository.findAllByItemIdInAndStatusNot(anyList(),
                any(BookingStatus.class))).thenReturn(bookings);

        List<ItemLogDto> result = itemService.getAllItemsByOwnerId(ownerId, from, size);
        item1.setLastBooking(booking1);
        item1.setNextBooking(booking2);

        assertEquals(ItemMapper.mapToListItemLogDto(items), result);

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRepository, times(1)).findByOwnerId(ownerId, pageable);
        verify(bookingRepository, times(1)).findAllByItemIdInAndStatusNot(anyList(),
                any(BookingStatus.class));
    }

    @Test
    public void shouldReturnEmptyArrayWhenGetAllItemsByOwnerIdIfNoItems() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(new User()));
        when(itemRepository.findByOwnerId(anyLong(), any(Pageable.class))).thenReturn(Collections.emptyList());

        List<ItemLogDto> result = itemService.getAllItemsByOwnerId(1L, 0, 2);

        assertEquals(Collections.emptyList(), result);

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findByOwnerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findAllByItemIdInAndStatusNot(anyList(),
                any(BookingStatus.class));
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionWhenGetAllItemsByEmptyOwnerId() {
        long ownerId = 1L;

        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                itemService.getAllItemsByOwnerId(ownerId, 0, 2));

        assertEquals("Владелец с id 1 не найден", exception.getMessage());

        verify(userRepository, times(1)).findById(ownerId);
        verify(itemRepository, never()).findByOwnerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findAllByItemIdInAndStatusNot(anyList(),
                any(BookingStatus.class));
    }

    @Test
    public void shouldReturnEmptyListWhenGetAllBySearchQueryIfTextEmptyOrNull() {
        assertEquals(Collections.emptyList(), itemService.getItemsBySearchQuery("    ", 0, 10));
        verify(itemRepository, never()).getItemsBySearchQuery(anyString(), any(Pageable.class));
        assertEquals(Collections.emptyList(), itemService.getItemsBySearchQuery(null, 0, 10));
        verify(itemRepository, never()).getItemsBySearchQuery(anyString(), any(Pageable.class));
    }

    @Test
    public void shouldAddComment() {
        long itemId = 1L;
        long authorId = 1L;
        LocalDateTime created = LocalDateTime.now();
        User author = new User(authorId, "user name", "user@email.com");
        Item item = new Item(itemId, "item name", "item description", true, author, null,
                null, Collections.emptyList(), null);
        CommentAddDto commentAddDto = new CommentAddDto("Хорошая вещь");
        Comment comment = new Comment(1L, "Хорошая вещь", itemId, author, created);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(), anyLong(),
                any(BookingStatus.class), any(LocalDateTime.class))).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentInItemLogDto result = itemService.addComment(commentAddDto, authorId, itemId);

        assertEquals(CommentMapper.mapToCommentInItemLogDto(comment), result);

        verify(itemRepository, times(1)).findById(itemId);
        verify(userRepository, times(1)).findById(authorId);
        verify(bookingRepository, times(1)).existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(),
                anyLong(), any(BookingStatus.class), any(LocalDateTime.class));
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionWhenAddCommentIfItemNotExist() {
        long itemId = 1L;
        long authorId = 1L;
        CommentAddDto commentAddDto = new CommentAddDto("Хорошая вещь");

        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                itemService.addComment(commentAddDto, authorId, itemId));
        assertEquals("Вещи с id 1 не существует", exception.getMessage());

        verify(itemRepository, times(1)).findById(itemId);
        verify(userRepository, never()).findById(authorId);
        verify(bookingRepository, never()).existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(),
                anyLong(), any(BookingStatus.class), any(LocalDateTime.class));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionWhenAddCommentIfUserNotExist() {
        long itemId = 1L;
        long authorId = 1L;
        CommentAddDto commentAddDto = new CommentAddDto("Хорошая вещь");
        Item item = new Item(itemId, "item name", "item description", true, new User(), null,
                null, Collections.emptyList(), null);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                itemService.addComment(commentAddDto, authorId, itemId));
        assertEquals("Пользователя с id 1 не существует", exception.getMessage());

        verify(itemRepository, times(1)).findById(itemId);
        verify(userRepository, times(1)).findById(authorId);
        verify(bookingRepository, never()).existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(),
                anyLong(), any(BookingStatus.class), any(LocalDateTime.class));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionWhenAddCommentIfItemBooked() {
        long itemId = 1L;
        long authorId = 1L;
        User author = new User(authorId, "user name", "user@email.com");
        CommentAddDto commentAddDto = new CommentAddDto("Хорошая вещь");
        Item item = new Item(itemId, "item name", "item description", true, author, null,
                null, Collections.emptyList(), null);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(author));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(),
                anyLong(), any(BookingStatus.class), any(LocalDateTime.class))).thenReturn(false);

        Exception exception = assertThrows(EntityNotAvailableException.class, () ->
                itemService.addComment(commentAddDto, authorId, itemId));
        assertEquals("Вы еще не арендовали эту вещь", exception.getMessage());

        verify(itemRepository, times(1)).findById(itemId);
        verify(userRepository, times(1)).findById(authorId);
        verify(bookingRepository, times(1)).existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(),
                anyLong(), any(BookingStatus.class), any(LocalDateTime.class));
        verify(commentRepository, never()).save(any(Comment.class));
    }

}



