package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository,
                           BookingRepository bookingRepository, CommentRepository commentRepository,
                           ItemRequestRepository itemRequestRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.itemRequestRepository = itemRequestRepository;
    }

    @Override
    @Transactional
    public ItemLogDto addItem(ItemAddDto itemAddDto, Long ownerId) {
        log.debug("Сервис - добавление item");
        User owner = userRepository.findById(ownerId).orElseThrow(() ->
                new EntityNotFoundException("Владелец с id " + ownerId + " не найден"));

        ItemRequest itemRequest = null;
        if (itemAddDto.getRequestId() != null) {
            itemRequest = itemRequestRepository.findById(itemAddDto.getRequestId()).orElse(null);
        }

        Item item = itemRepository.save(ItemMapper.mapToItem(itemAddDto, owner, itemRequest));
        return ItemMapper.mapToItemLogDto(item);
    }

    @Override
    @Transactional
    public ItemLogDto updateItem(ItemUpdateDto itemUpdateDto, Long itemId, Long ownerId) {
        log.debug("Сервис - обновление item с id {}", itemId);
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new EntityNotFoundException("Item с id " + itemId + " не найден"));

        User owner = userRepository.findById(ownerId).orElseThrow(() ->
                new EntityNotFoundException("Владелец с id " + ownerId + " не найден"));

        if (!isOwnerCorrect(owner, item)) {
            throw new EntityNotFoundException("Владелец c id " + ownerId + " у item с id " + itemId + " не найден");
        }
        if (itemUpdateDto.getName() != null) {
            item.setName(itemUpdateDto.getName());
        }
        if (itemUpdateDto.getDescription() != null) {
            item.setDescription(itemUpdateDto.getDescription());
        }
        if (itemUpdateDto.getAvailable() != null) {
            item.setAvailable(itemUpdateDto.getAvailable());
        }
        return ItemMapper.mapToItemLogDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemLogDto getItemById(Long itemId, Long ownerId) {
        log.debug("Сервис -получение item по id {}", itemId);
        log.debug("Проверка item с id {} на существование", itemId);

        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new EntityNotFoundException("item с id " + itemId + " не найден"));

        if (Objects.equals(item.getOwner().getId(), ownerId)) {
            item.setLastBooking(bookingRepository
                    .findFirst1ByItemIdAndStartIsBeforeAndStatusNotOrderByStartDesc(
                            item.getId(), LocalDateTime.now(), BookingStatus.REJECTED));
            item.setNextBooking(bookingRepository
                    .findFirst1ByItemIdAndStartIsAfterAndStatusNotOrderByStart(
                            item.getId(), LocalDateTime.now(), BookingStatus.REJECTED));
        }
        return ItemMapper.mapToItemLogDto(item);
    }

    @Override
    @Transactional
    public List<ItemLogDto> getAllItemsByOwnerId(Long ownerId, int from, int size) {
        log.debug("Сервис - получение списка всех items для пользователя с id {}", ownerId);

        userRepository.findById(ownerId).orElseThrow(() ->
                new EntityNotFoundException("Владелец с id " + ownerId + " не найден"));

        Sort sort = Sort.by(Sort.Order.asc("id"));
        Pageable pageable = PageRequest.of(from / size, size, sort);

        List<Item> items = itemRepository.findByOwnerId(ownerId, pageable);
        if (items == null || items.size() == 0) {
            return Collections.emptyList();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());
        List<Booking> bookings = bookingRepository.findAllByItemIdInAndStatusNot(itemIds,
                BookingStatus.REJECTED);

        return items.stream()
                .map(item -> {
                    Booking lastBooking = bookings.stream()
                            .filter(booking -> Objects.equals(booking.getItem().getId(), item.getId()) &&
                                    booking.getStart().isBefore(LocalDateTime.now()))
                            .max(Comparator.comparing(Booking::getStart))
                            .orElse(null);

                    Booking nextBooking = bookings.stream()
                            .filter(booking -> Objects.equals(booking.getItem().getId(), item.getId()) &&
                                    booking.getStart().isAfter(LocalDateTime.now()))
                            .min(Comparator.comparing(Booking::getStart))
                            .orElse(null);

                    item.setLastBooking(lastBooking);
                    item.setNextBooking(nextBooking);

                    return ItemMapper.mapToItemLogDto(item);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteItemById(Long itemId) {
        log.debug("Сервис - удаление item по id {}", itemId);
        itemRepository.deleteById(itemId);
    }

    @Override
    @Transactional
    public List<ItemLogDto> getItemsBySearchQuery(String text, int from, int size) {
        log.debug("Сервис - получение списка всех items, содержащих подстроку {}", text);
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        Pageable pageable = PageRequest.of(from / size, size);
        return ItemMapper.mapToListItemLogDto(itemRepository.getItemsBySearchQuery(text, pageable));
    }

    @Override
    @Transactional
    public CommentInItemLogDto addComment(CommentAddDto commentAddDto, Long authorId, Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new EntityNotFoundException("Вещи с id " + itemId + " не существует"));

        User author = userRepository.findById(authorId).orElseThrow(() ->
                new EntityNotFoundException("Пользователя с id " + authorId + " не существует"));

        if (!bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(authorId, itemId,
                BookingStatus.APPROVED, LocalDateTime.now())) {
            throw new EntityNotAvailableException("Вы еще не арендовали эту вещь");
        }

        Comment comment = CommentMapper.mapToComment(commentAddDto, itemId, author);

        return CommentMapper.mapToCommentInItemLogDto(commentRepository.save(comment));
    }

    private boolean isOwnerCorrect(User owner, Item item) {
        log.debug("Проверка, что переданный владелец существует у item");
        return Objects.equals(item.getOwner().getId(), owner.getId());
    }
}
