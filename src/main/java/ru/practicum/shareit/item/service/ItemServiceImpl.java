package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    public ItemServiceImpl(ItemStorage itemStorage, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, Long ownerId) {
        log.debug("Сервис - добавление item");
        if (isOwnerEmpty(ownerId)) {
            throw new EntityNotFoundException("Владелец с id " + ownerId + " не найден");
        }
        return itemStorage.addItem(itemDto, ownerId);
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long itemId, Long ownerId) {
        log.debug("Сервис - обновление item с id {}", itemId);
        ItemDto itemBeforeUpdate = getItemById(itemId);
        if (!isOwnerCorrect(ownerId, itemBeforeUpdate)) {
            throw new EntityNotFoundException("Владелец c id " + ownerId + " у item с id " + itemId + " не найден");
        }
        return itemStorage.updateItem(itemDto, itemId, ownerId);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        log.debug("Сервис -получение item по id {}", itemId);
        ItemDto itemDto = itemStorage.getItemById(itemId);
        log.debug("Проверка item с id {} на существование", itemId);
        if (itemDto == null) {
            throw new EntityNotFoundException("item с id " + itemId + " не найден");
        }
        return itemDto;
    }

    @Override
    public List<ItemDto> getAllItemsByOwnerId(Long ownerId) {
        log.debug("Сервис - получение списка всех items для пользователя с id {}", ownerId);
        if (isOwnerEmpty(ownerId)) {
            throw new EntityNotFoundException("Владелец с id " + ownerId + " не найден");
        }
        return itemStorage.getAllItemsByOwnerId(ownerId);
    }

    @Override
    public void deleteItemById(Long itemId) {
        log.debug("Сервис - удаление item по id {}", itemId);
        itemStorage.deleteItemById(itemId);
    }

    @Override
    public void deleteAllItemsByOwnerId(Long ownerId) {
        log.debug("Сервис - удаление всех items для пользователя с id {}", ownerId);
        if (isOwnerEmpty(ownerId)) {
            throw new EntityNotFoundException("Владелец с id " + ownerId + " не найден");
        }
        itemStorage.deleteAllItemsByOwnerId(ownerId);
    }

    @Override
    public List<ItemDto> getItemsBySearchQuery(String text) {
        log.debug("Сервис - получение списка всех items, содержащих подстроку {}", text);
        return itemStorage.getItemsBySearchQuery(textForSearchToLowerCase(text));
    }

    private boolean isOwnerEmpty(Long ownerId) {
        log.debug("Проверка пользователя на существование");
        return userStorage.getUserById(ownerId) == null;
    }

    private boolean isOwnerCorrect(Long ownerId, ItemDto itemDto) {
        log.debug("Проверка, что переданный владелец существует у item");
        return Objects.equals(itemDto.getOwnerId(), ownerId);
    }

    private String textForSearchToLowerCase(String text) {
        return !text.isBlank() ? text.toLowerCase() : null;
    }
}
