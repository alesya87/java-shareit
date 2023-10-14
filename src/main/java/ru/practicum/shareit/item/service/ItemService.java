package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(ItemDto itemDto, Long itemId, Long ownerId);

    ItemDto getItemById(Long itemId);

    List<ItemDto> getAllItemsByOwnerId(Long ownerId);

    void deleteItemById(Long itemId);

    void deleteAllItemsByOwnerId(Long ownerId);

    List<ItemDto> getItemsBySearchQuery(String text);
}
