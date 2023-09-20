package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item addItem(Item item);

    Item updateItem(Item item);

    Item getItemById(Long itemId);

    List<Item> getAllItemsByOwnerId(Long ownerId);

    void deleteItemById(Long itemId);

    void deleteAllItemsByOwnerId(Long ownerId);

    List<Item> getItemsBySearchQuery(String text);

}
