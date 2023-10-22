package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestAddDto;
import ru.practicum.shareit.request.dto.ItemRequestLogDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    public static ItemRequest mapToItemRequest(ItemRequestAddDto itemRequestAddDto, User requester) {
        return ItemRequest.builder()
                .description(itemRequestAddDto.getDescription())
                .requester(requester)
                .created(LocalDateTime.now())
                .build();
    }

    public static ItemRequestLogDto mapToItemRequestLogDto(ItemRequest itemRequest) {
        return ItemRequestLogDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(itemRequest.getItems() == null ? Collections.emptyList() :
                        ItemMapper.mapToListItemInRequestLogDto(itemRequest.getItems()))
                .build();
    }

    public static List<ItemRequestLogDto> mapToListItemRequestLogDto(List<ItemRequest> itemRequests) {
        return itemRequests.stream()
                .map(ItemRequestMapper::mapToItemRequestLogDto)
                .collect(Collectors.toList());
    }
}
