package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.item.dto.ItemAddDto;
import ru.practicum.shareit.item.dto.ItemLogDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {
    public static Item mapToItem(ItemAddDto itemAddDto, Long ownerId) {
        return Item.builder()
                .available(itemAddDto.getAvailable())
                .description(itemAddDto.getDescription())
                .name(itemAddDto.getName())
                .ownerId(ownerId)
                .build();
    }

    public static Item mapToItem(ItemUpdateDto itemUpdateDto, Long itemId, Long ownerId) {
        return Item.builder()
                .id(itemId)
                .available(itemUpdateDto.getAvailable())
                .description(itemUpdateDto.getDescription())
                .name(itemUpdateDto.getName())
                .ownerId(ownerId)
                .build();
    }

    public static ItemLogDto mapToItemLogDto(Item item) {
        return ItemLogDto.builder()
                .id(item.getId())
                .available(item.getAvailable())
                .description(item.getDescription())
                .name(item.getName())
                .ownerId(item.getOwnerId())
                .nextBooking(item.getNextBooking() != null ?
                        BookingMapper.mapToBookingShortDto(item.getNextBooking())
                        : null)
                .lastBooking(item.getLastBooking() != null ? BookingMapper.mapToBookingShortDto(item.getLastBooking())
                        : null)
                .comments(item.getComments())
                .build();
    }

    public static List<ItemLogDto> mapToListItemLogDto(List<Item> items) {
        return items.stream()
                .map(ItemMapper::mapToItemLogDto)
                .collect(Collectors.toList());
    }
}
