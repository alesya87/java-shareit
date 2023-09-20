package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ItemStorageImpl implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private long id = 0;

    @Override
    public Item addItem(Item item) {
        log.info("Добавление item в хранилище");
        items.put(++id, item);
        item.setId(id);
        log.info("Item с id " + id + " добавлен в хранилище");
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        log.info("Обновление item с id " + item.getId() + " в хранилище");
        Item itemBeforeUpdate = items.get(item.getId() );
        item.setName(item.getName() != null ? item.getName() : itemBeforeUpdate.getName());
        item.setDescription(item.getDescription() != null ? item.getDescription() : itemBeforeUpdate.getDescription());
        item.setAvailable(item.getAvailable() != null ? item.getAvailable() : itemBeforeUpdate.getAvailable());
        items.put(item.getId(), item);
        log.info("Пользователь с id " + item.getId() + " обновлен в хранилище");
        return item;
    }

    @Override
    public Item getItemById(Long itemId) {
        log.info("Получение item по id " + id + " из хранилища");
        return items.get(id);
    }

    @Override
    public List<Item> getAllItemsByOwnerId(Long ownerId) {
        log.info("Получение всех items по ownerId " + ownerId + " из хранилища");
        return new ArrayList<>(items.values()).stream()
                .filter(item -> Objects.equals(item.getOwnerId(), ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteItemById(Long itemId) {
        log.info("Удаление item по id " + itemId + " из хранилища");
        items.remove(itemId);
    }

    @Override
    public void deleteAllItemsByOwnerId(Long ownerId) {
        log.info("Удаление всех items по ownerId " + ownerId + " из хранилища");
        for (Item item : items.values()) {
            if (Objects.equals(item.getOwnerId(), ownerId)) {
                items.remove(item.getId());
            }
        }
    }

    @Override
    public List<Item> getItemsBySearchQuery(String text) {
        log.info("Получение списка всех items, содержащих подстроку " + text + " из хранилища");
        if (text == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(items.values()).stream()
                .filter(item -> item.getName().toLowerCase().contains(text) ||
                        item.getDescription().toLowerCase().contains(text) &&
                                item.getAvailable())
                .collect(Collectors.toList());
    }
}
