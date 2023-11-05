package ru.practicum.shareit.item.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentAddDto;
import ru.practicum.shareit.item.comment.dto.CommentInItemLogDto;
import ru.practicum.shareit.item.dto.ItemAddDto;
import ru.practicum.shareit.item.dto.ItemLogDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {
    private static final String OWNER_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemLogDto addItem(@RequestBody ItemAddDto itemAddDto, @RequestHeader(OWNER_HEADER) Long ownerId) {
        log.info("Получен POST-запрос к эндпоинту: '/items' на добавление item: " +
                        "name: {}, description: {}, isAvailable: {}, ownerId: {}",
                itemAddDto.getName(), itemAddDto.getDescription(), itemAddDto.getAvailable(), ownerId);
        return itemService.addItem(itemAddDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemLogDto updateItem(@RequestBody ItemUpdateDto itemUpdateDto, @PathVariable Long itemId,
                                 @RequestHeader(OWNER_HEADER) Long ownerId) {
        log.info("Получен PATCH-запрос к эндпоинту: '/items' на обновление item с id {} - name: {}," +
                        "description: {}, isAvailable: {}, ownerId: {}",
                itemId, itemUpdateDto.getName(), itemUpdateDto.getDescription(), itemUpdateDto.getAvailable(), ownerId);
        return itemService.updateItem(itemUpdateDto, itemId, ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemLogDto getItemById(@PathVariable Long itemId, @RequestHeader(OWNER_HEADER) Long ownerId) {
        log.info("Получен GET-запрос к эндпоинту: '/items/{itemId}' на получение item по id {}", itemId);
        return itemService.getItemById(itemId, ownerId);
    }

    @GetMapping
    public List<ItemLogDto> getAllItemsByOwnerId(@RequestHeader(OWNER_HEADER) Long ownerId,
                                                 @RequestParam(defaultValue = "0") int from,
                                                 @RequestParam(defaultValue = "10") int size) {
        log.info("Получен GET-запрос к эндпоинту: '/items' на получение списка всех items для пользователя с id {}", ownerId);
        return itemService.getAllItemsByOwnerId(ownerId, from, size);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItemById(@PathVariable Long itemId) {
        log.info("Получен DELETE-запрос к эндпоинту: '/items/{userId}' на удаление item по id {}", itemId);
        itemService.deleteItemById(itemId);
    }

    @GetMapping("/search")
    public List<ItemLogDto> getItemsBySearchQuery(@RequestParam(required = false) String text,
                                                  @RequestParam(defaultValue = "0") int from,
                                                  @RequestParam(defaultValue = "10") int size) {
        log.info("Получен GET-запрос к эндпоинту: '/items/search' на получение списка всех items, содержащих подстроку {}", text);
        return itemService.getItemsBySearchQuery(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentInItemLogDto addComment(@RequestBody CommentAddDto comment,
                                          @RequestHeader(OWNER_HEADER) Long authorId,
                                          @PathVariable Long itemId) {
        log.info("Получен POST-запрос к эндпоинту: '/items//{itemId}/comment' на добавление комментария для вещи {}", itemId);
        return itemService.addComment(comment, authorId, itemId);
    }
}
