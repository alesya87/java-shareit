package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository,
                           BookingRepository bookingRepository, CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public ItemLogDto addItem(ItemAddDto itemAddDto, Long ownerId) {
        log.debug("Сервис - добавление item");
        if (isOwnerEmpty(ownerId)) {
            throw new EntityNotFoundException("Владелец с id " + ownerId + " не найден");
        }
        return ItemMapper.mapToItemLogDto(itemRepository.save(ItemMapper.mapToItem(itemAddDto, ownerId)));
    }

    @Override
    public ItemLogDto updateItem(ItemUpdateDto itemUpdateDto, Long itemId, Long ownerId) {
        log.debug("Сервис - обновление item с id {}", itemId);
        Item itemBeforeUpdate = itemRepository.findById(itemId).orElse(null);
        if (!isOwnerCorrect(ownerId, itemBeforeUpdate)) {
            throw new EntityNotFoundException("Владелец c id " + ownerId + " у item с id " + itemId + " не найден");
        }
        if (itemUpdateDto.getName() == null) {
            itemUpdateDto.setName(itemBeforeUpdate.getName());
        }
        if (itemUpdateDto.getDescription() == null) {
            itemUpdateDto.setDescription(itemBeforeUpdate.getDescription());
        }
        if (itemUpdateDto.getAvailable() == null) {
            itemUpdateDto.setAvailable(itemBeforeUpdate.getAvailable());
        }
        return ItemMapper.mapToItemLogDto(itemRepository.save(ItemMapper.mapToItem(itemUpdateDto, itemId, ownerId)));
    }

    @Override
    public ItemLogDto getItemById(Long itemId, Long ownerId) {
        log.debug("Сервис -получение item по id {}", itemId);
        log.debug("Проверка item с id {} на существование", itemId);

        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            throw new EntityNotFoundException("item с id " + itemId + " не найден");
        }
        setItemBooking(item, ownerId);
        setItemComments(item);

        return ItemMapper.mapToItemLogDto(item);
    }

    @Override
    public List<ItemLogDto> getAllItemsByOwnerId(Long ownerId) {
        log.debug("Сервис - получение списка всех items для пользователя с id {}", ownerId);
        if (isOwnerEmpty(ownerId)) {
            throw new EntityNotFoundException("Владелец с id " + ownerId + " не найден");
        }
        List<Item> items = itemRepository.findByOwnerIdOrderById(ownerId);
        for (Item item : items) {
            setItemBooking(item, ownerId);
            setItemComments(item);
        }
        return ItemMapper.mapToListItemLogDto(items);
    }

    @Override
    public void deleteItemById(Long itemId) {
        log.debug("Сервис - удаление item по id {}", itemId);
        itemRepository.deleteById(itemId);
    }

    @Override
    public List<ItemLogDto> getItemsBySearchQuery(String text) {
        log.debug("Сервис - получение списка всех items, содержащих подстроку {}", text);
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return ItemMapper.mapToListItemLogDto(itemRepository.getItemsBySearchQuery(text));
    }

    @Override
    public CommentInItemLogDto addComment(CommentAddDto commentAddDto, Long authorId, Long itemId) {
        Item item = itemRepository.findById(itemId).orElse(null);
        User author = userRepository.findById(authorId).orElse(null);

        if (item == null) {
            throw new EntityNotFoundException("Вещи с id " + itemId + " не существует");
        }

        if (author == null) {
            throw new EntityNotFoundException("Пользователя с id " + authorId + " не существует");
        }

        if (!bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(authorId, itemId,
                BookingStatus.APPROVED, LocalDateTime.now())) {
            throw new EntityNotAvailableException("Вы еще не арендовали эту вещь");
        }

        Comment comment = Comment.builder()
                .text(commentAddDto.getText())
                .itemId(itemId)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        return CommentMapper.mapToCommentInItemLogDto(commentRepository.save(comment));
    }

    private boolean isOwnerEmpty(Long ownerId) {
        log.debug("Проверка пользователя на существование");
        return !userRepository.existsById(ownerId);
    }

    private Item setItemBooking(Item item, Long ownerId) {
        if (Objects.equals(item.getOwnerId(), ownerId)) {
            item.setLastBooking(bookingRepository
                    .findFirst1ByItemIdAndStartIsBeforeAndStatusNotOrderByStartDesc(
                            item.getId(), LocalDateTime.now(), BookingStatus.REJECTED));
            item.setNextBooking(bookingRepository
                    .findFirst1ByItemIdAndStartIsAfterAndStatusNotOrderByStart(
                            item.getId(), LocalDateTime.now(), BookingStatus.REJECTED));
        }
        return item;
    }

    private Item setItemComments(Item item) {
        List<Comment> comments = commentRepository.findAllByItemId(item.getId());
        item.setComments(comments.size() != 0 ? CommentMapper.mapToListCommentInItemLogDto(comments) :
                Collections.emptyList());
        return item;
    }

    private boolean isOwnerCorrect(Long ownerId, Item item) {
        log.debug("Проверка, что переданный владелец существует у item");
        return Objects.equals(item.getOwnerId(), ownerId);
    }
}
