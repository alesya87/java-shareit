package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

public class ItemMapper {
    public static Item mapToItem(ItemDto itemDto, Long ownerId) {
        return Item.builder()
                .id(itemDto.getId())
                .available(itemDto.getAvailable())
                .description(itemDto.getDescription())
                .name(itemDto.getName())
                .ownerId(ownerId)
                .build();
    }

    public static ItemDto mapToItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .available(item.getAvailable())
                .description(item.getDescription())
                .name(item.getName())
                .ownerId(item.getOwnerId())
                .build();
    }
}
