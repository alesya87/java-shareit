package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestAddDto;
import ru.practicum.shareit.request.dto.ItemRequestLogDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestLogDto addItemRequest(ItemRequestAddDto itemRequestAddDto, Long requesterId);

    List<ItemRequestLogDto> getAllItemRequestsByUserId(Long userId);

    List<ItemRequestLogDto> getAllItemRequests(Long requesterId, int from, int size);

    ItemRequestLogDto getItemRequestById(Long userId, Long id);
}
