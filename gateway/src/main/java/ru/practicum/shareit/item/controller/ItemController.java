package ru.practicum.shareit.item.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.comment.dto.CommentAddDto;
import ru.practicum.shareit.item.dto.ItemAddDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@Slf4j
@RestController
@RequestMapping("/items")
@Validated
public class ItemController {
    private static final String OWNER_HEADER = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    public ItemController(ItemClient itemClient) {
        this.itemClient = itemClient;
    }

    @PostMapping
    public ResponseEntity<Object> addItem(@Valid @RequestBody ItemAddDto itemAddDto, @RequestHeader(OWNER_HEADER) Long ownerId) {
        log.info("Получен POST-запрос к эндпоинту: '/items' на добавление item: " +
                        "name: {}, description: {}, isAvailable: {}, ownerId: {}",
                itemAddDto.getName(), itemAddDto.getDescription(), itemAddDto.getAvailable(), ownerId);
        return itemClient.addItem(itemAddDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestBody ItemUpdateDto itemUpdateDto, @PathVariable Long itemId,
                                 @RequestHeader(OWNER_HEADER) Long ownerId) {
        log.info("Получен PATCH-запрос к эндпоинту: '/items' на обновление item с id {} - name: {}," +
                        "description: {}, isAvailable: {}, ownerId: {}",
                itemId, itemUpdateDto.getName(), itemUpdateDto.getDescription(), itemUpdateDto.getAvailable(), ownerId);
        return itemClient.updateItem(itemUpdateDto, itemId, ownerId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable Long itemId, @RequestHeader(OWNER_HEADER) Long ownerId) {
        log.info("Получен GET-запрос к эндпоинту: '/items/{itemId}' на получение item по id {}", itemId);
        return itemClient.getItemById(itemId, ownerId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemsByOwnerId(@RequestHeader(OWNER_HEADER) Long ownerId,
                                                 @Valid @RequestParam(defaultValue = "0") @Min(value = 0) int from,
                                                 @Valid @RequestParam(defaultValue = "10") @Min(value = 1) int size) {
        log.info("Получен GET-запрос к эндпоинту: '/items' на получение списка всех items для пользователя с id {}", ownerId);
        return itemClient.getAllItemsByOwnerId(ownerId, from, size);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItemById(@PathVariable Long itemId) {
        log.info("Получен DELETE-запрос к эндпоинту: '/items/{userId}' на удаление item по id {}", itemId);
        itemClient.deleteItemById(itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getItemsBySearchQuery(@RequestParam(required = false) String text,
                                                  @Valid @RequestParam(defaultValue = "0") @Min(value = 0) int from,
                                                  @Valid @RequestParam(defaultValue = "10") @Min(value = 1) int size) {
        log.info("Получен GET-запрос к эндпоинту: '/items/search' на получение списка всех items, содержащих подстроку {}", text);
        return itemClient.getItemsBySearchQuery(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@Valid @RequestBody CommentAddDto comment,
                                          @RequestHeader(OWNER_HEADER) Long authorId,
                                          @PathVariable Long itemId) {
        log.info("Получен POST-запрос к эндпоинту: '/items//{itemId}/comment' на добавление комментария для вещи {}", itemId);
        return itemClient.addComment(comment, authorId, itemId);
    }
}
