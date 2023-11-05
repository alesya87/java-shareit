package ru.practicum.shareit.request.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestAddDto;
import ru.practicum.shareit.request.dto.ItemRequestLogDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    private static final String REQUESTER_HEADER = "X-Sharer-User-Id";

    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public ItemRequestLogDto addItemRequest(@RequestBody ItemRequestAddDto itemRequestAddDto,
                                            @RequestHeader(REQUESTER_HEADER) Long requesterId) {
        log.info("Получен POST-запрос к эндпоинту: '/requests' на добавление запроса от ползователя {}", requesterId);
        return itemRequestService.addItemRequest(itemRequestAddDto, requesterId);
    }

    @GetMapping
    public List<ItemRequestLogDto> getAllItemRequestsByUserId(@RequestHeader(REQUESTER_HEADER) Long requesterId) {
        log.info("Получен GET-запрос к эндпоинту: '/requests' на просмотр своих запросов от ползователя {}", requesterId);
        return itemRequestService.getAllItemRequestsByUserId(requesterId);
    }

    @GetMapping("/all")
    public List<ItemRequestLogDto> getAllItemRequests(@RequestHeader(REQUESTER_HEADER) Long requesterId,
                                                      @RequestParam(defaultValue = "0") int from,
                                                      @RequestParam(defaultValue = "10") int size) {
        log.info("Получен GET-запрос к эндпоинту: '/requests/all' на просмотр всех запросов от ползователя {}", requesterId);
        return itemRequestService.getAllItemRequests(requesterId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestLogDto getItemRequestById(@RequestHeader(REQUESTER_HEADER) Long userId,
                                                @PathVariable Long requestId) {
        log.info("Получен GET-запрос к эндпоинту: '/requests/{requestId}' " +
                "на просмотр запроса с id {} от ползователя {}", requestId, userId);
        return itemRequestService.getItemRequestById(userId, requestId);
    }
}
