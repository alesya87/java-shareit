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
        log.info("Сервис - добавление item");
        if (isOwnerEmpty(ownerId)) {
            throw new EntityNotFoundException("Владелец с id " + ownerId + " не найден");
        }
        return ItemMapper.mapToItemDto(itemStorage.addItem(ItemMapper.mapToItem(itemDto, ownerId)));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long itemId, Long ownerId) {
        log.info("Сервис - обновление item с id " + itemId);
        ItemDto itemBeforeUpdate = getItemById(itemId);
        if (!isOwnerCorrect(ownerId, itemBeforeUpdate)) {
            throw new EntityNotFoundException("Владелец c id " + ownerId + " у item с id " + itemId + " не найден");
        }
        itemDto.setId(itemId);
        return ItemMapper.mapToItemDto(itemStorage.updateItem(ItemMapper.mapToItem(itemDto, ownerId)));
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        log.info("Сервис -получение item по id " + itemId);
        Item item = itemStorage.getItemById(itemId);
        log.info("Проверка item с id " + itemId + " на существование");
        if (item == null) {
            throw new EntityNotFoundException("item с id " + itemId + " не найден");
        }
        return ItemMapper.mapToItemDto(item);
    }

    @Override
    public List<ItemDto> getAllItemsByOwnerId(Long ownerId) {
        log.info("Сервис - получение списка всех items для пользователя с id " + ownerId);
        if (isOwnerEmpty(ownerId)) {
            throw new EntityNotFoundException("Владелец с id " + ownerId + " не найден");
        }
        return itemStorage.getAllItemsByOwnerId(ownerId).stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteItemById(Long itemId) {
        log.info("Сервис - удаление item по id " + itemId);
        itemStorage.deleteItemById(itemId);
    }

    @Override
    public void deleteAllItemsByOwnerId(Long ownerId) {
        log.info("Сервис - удаление всех items для пользователя с id " + ownerId);
        if (isOwnerEmpty(ownerId)) {
            throw new EntityNotFoundException("Владелец с id " + ownerId + " не найден");
        }
        itemStorage.deleteAllItemsByOwnerId(ownerId);
    }

    @Override
    public List<ItemDto> getItemsBySearchQuery(String text) {
        log.info("Сервис - получение списка всех items, содержащих подстроку " + text);
        return itemStorage.getItemsBySearchQuery(textForSearchToLowerCase(text)).stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }

    private boolean isOwnerEmpty(Long ownerId) {
        log.info("Проверка полльзователя на существование");
        return userStorage.getUserById(ownerId) == null;
    }

    private boolean isOwnerCorrect(Long ownerId, ItemDto itemDto) {
        log.info("Проверка, что переданный владелец существует у item");
        return Objects.equals(itemDto.getOwnerId(), ownerId);
    }

    private String textForSearchToLowerCase(String text) {
        return !text.isBlank() ? text.toLowerCase() : null;
    }
}
