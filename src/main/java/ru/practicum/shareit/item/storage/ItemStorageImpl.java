package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ItemStorageImpl implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private long id = 0;

    @Override
    public ItemDto addItem(ItemDto itemDto, Long ownerId) {
        log.debug("Добавление item в хранилище");
        Item item = ItemMapper.mapToItem(itemDto, ownerId);
        item.setId(++id);
        items.put(item.getId(), item);
        log.debug("Item с id {} добавлен в хранилище", item.getId());
        return ItemMapper.mapToItemDto(item);
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long itemId, Long ownerId) {
        log.debug("Обновление item с id {} в хранилище", itemId);
        Item item = items.get(itemId);
        item.setName(itemDto.getName() != null ? itemDto.getName() : item.getName());
        item.setDescription(itemDto.getDescription() != null ? itemDto.getDescription() : item.getDescription());
        item.setAvailable(itemDto.getAvailable() != null ? itemDto.getAvailable() : item.getAvailable());
        items.put(itemId, item);
        log.debug("Пользователь с id {} обновлен в хранилище", itemId);
        return ItemMapper.mapToItemDto(item);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        log.debug("Получение item по id {} из хранилища", id);
        Item item = items.get(id);
        return item != null ? ItemMapper.mapToItemDto(item) : null;
    }

    @Override
    public List<ItemDto> getAllItemsByOwnerId(Long ownerId) {
        log.debug("Получение всех items по ownerId {} из хранилища", ownerId);
        return new ArrayList<>(items.values()).stream()
                .filter(item -> Objects.equals(item.getOwnerId(), ownerId))
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteItemById(Long itemId) {
        log.debug("Удаление item по id {} из хранилища", itemId);
        items.remove(itemId);
    }

    @Override
    public void deleteAllItemsByOwnerId(Long ownerId) {
        log.debug("Удаление всех items по ownerId {} из хранилища", ownerId);
        for (Item item : items.values()) {
            if (Objects.equals(item.getOwnerId(), ownerId)) {
                items.remove(item.getId());
            }
        }
    }

    @Override
    public List<ItemDto> getItemsBySearchQuery(String text) {
        log.debug("Получение списка всех items, содержащих подстроку {} из хранилища", text);
        if (text == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(items.values()).stream()
                .filter(item -> item.getName().toLowerCase().contains(text) ||
                        item.getDescription().toLowerCase().contains(text) &&
                                item.getAvailable())
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }
}
