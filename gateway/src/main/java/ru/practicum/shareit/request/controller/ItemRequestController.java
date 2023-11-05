package ru.practicum.shareit.request.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestAddDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    private static final String REQUESTER_HEADER = "X-Sharer-User-Id";

    public ItemRequestController(ItemRequestClient itemRequestClient) {
        this.itemRequestClient = itemRequestClient;
    }

    @PostMapping
    public ResponseEntity<Object> addItemRequest(@Valid @RequestBody ItemRequestAddDto itemRequestAddDto,
                                         @RequestHeader(REQUESTER_HEADER) Long requesterId) {
        log.info("Получен POST-запрос к эндпоинту: '/requests' на добавление запроса от ползователя {}", requesterId);
        return itemRequestClient.addItemRequest(itemRequestAddDto, requesterId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemRequestsByUserId(@RequestHeader(REQUESTER_HEADER) Long requesterId) {
        log.info("Получен GET-запрос к эндпоинту: '/requests' на просмотр своих запросов от ползователя {}", requesterId);
        return itemRequestClient.getAllItemRequestsByUserId(requesterId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequests(@RequestHeader(REQUESTER_HEADER) Long requesterId,
                                                      @Valid @RequestParam(defaultValue = "0") @Min(value = 0) int from,
                                                      @Valid @RequestParam(defaultValue = "10") @Min(value = 1) int size) {
        log.info("Получен GET-запрос к эндпоинту: '/requests/all' на просмотр всех запросов от ползователя {}", requesterId);
        return itemRequestClient.getAllItemRequests(requesterId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestById(@RequestHeader(REQUESTER_HEADER) Long userId,
                                                @PathVariable Long requestId) {
        log.info("Получен GET-запрос к эндпоинту: '/requests/{requestId}' " +
                "на просмотр запроса с id {} от ползователя {}", requestId, userId);
        return itemRequestClient.getItemRequestById(userId, requestId);
    }
}
