package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemAddDto;
import ru.practicum.shareit.item.dto.ItemLogDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;


    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository,
                           BookingRepository bookingRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
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

    private boolean isOwnerEmpty(Long ownerId) {
        log.debug("Проверка пользователя на существование");
        return !userRepository.existsById(ownerId);
    }

    private Item setItemBooking(Item item, Long ownerId) {
        if (Objects.equals(item.getOwnerId(), ownerId)) {
            item.setLastBooking(bookingRepository
                    .findFirst1ByItemIdAndStartIsBeforeAndStatusNotOrderByStart(
                            item.getId(), LocalDateTime.now(), BookingStatus.REJECTED));
            item.setNextBooking(bookingRepository
                    .findFirst1ByItemIdAndStartIsAfterAndStatusNotOrderByStart(
                            item.getId(), LocalDateTime.now(), BookingStatus.REJECTED));
        }
        return item;
    }

    private boolean isOwnerCorrect(Long ownerId, Item item) {
        log.debug("Проверка, что переданный владелец существует у item");
        return Objects.equals(item.getOwnerId(), ownerId);
    }
}
