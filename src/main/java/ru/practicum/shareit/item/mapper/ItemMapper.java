package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.dto.ItemAddDto;
import ru.practicum.shareit.item.dto.ItemInRequestLogDto;
import ru.practicum.shareit.item.dto.ItemLogDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {
    public static Item mapToItem(ItemAddDto itemAddDto, Long ownerId, ItemRequest itemRequest) {
        return Item.builder()
                .available(itemAddDto.getAvailable())
                .description(itemAddDto.getDescription())
                .name(itemAddDto.getName())
                .ownerId(ownerId)
                .itemRequest(itemRequest)
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
                .comments(item.getComments() != null ? CommentMapper.mapToListCommentInItemLogDto(item.getComments())
                        : Collections.emptyList())
                .requestId(item.getItemRequest() != null ? item.getItemRequest().getId() : null)
                .build();
    }

    public static List<ItemLogDto> mapToListItemLogDto(List<Item> items) {
        return items.stream()
                .map(ItemMapper::mapToItemLogDto)
                .collect(Collectors.toList());
    }

    public static ItemInRequestLogDto mapToItemInRequestLogDto(Item item) {
        return ItemInRequestLogDto.builder()
                .id(item.getId())
                .name(item.getName())
                .available(item.getAvailable())
                .requestId(item.getItemRequest().getId())
                .description(item.getDescription())
                .build();
    }

    public static List<ItemInRequestLogDto> mapToListItemInRequestLogDto(List<Item> items) {
        return items.stream()
                .map(ItemMapper::mapToItemInRequestLogDto)
                .collect(Collectors.toList());
    }
}
