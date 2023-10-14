package ru.practicum.shareit.item.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
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
    public ItemDto addItem(@Valid @RequestBody ItemDto itemDto, @Valid @NotBlank @RequestHeader(OWNER_HEADER) Long ownerId) {
        log.info("Получен POST-запрос к эндпоинту: '/items' на добавление item: " +
                "name: {}, description: {}, isAvailable: {}, ownerId: {}",
                itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable(), ownerId);
        return itemService.addItem(itemDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestBody ItemDto itemDto, @Valid @PathVariable @NotBlank Long itemId,
                              @Valid @NotBlank @RequestHeader(OWNER_HEADER) Long ownerId) {
        log.info("Получен PATCH-запрос к эндпоинту: '/items' на обновление item с id {} - name: {}," +
                        "description: {}, isAvailable: {}, ownerId: {}",
                itemId, itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable(), ownerId);
        return itemService.updateItem(itemDto, itemId, ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@Valid @PathVariable @NotBlank Long itemId) {
        log.info("Получен GET-запрос к эндпоинту: '/items/{itemId}' на получение item по id {}", itemId);
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<ItemDto> getAllItemsByOwnerId(@Valid @NotBlank @RequestHeader(OWNER_HEADER) Long ownerId) {
        log.info("Получен GET-запрос к эндпоинту: '/items' на получение списка всех items для пользователя с id {}", ownerId);
        return itemService.getAllItemsByOwnerId(ownerId);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItemById(@PathVariable Long itemId) {
        log.info("Получен DELETE-запрос к эндпоинту: '/items/{userId}' на удаление item по id {}", itemId);
        itemService.deleteItemById(itemId);
    }

    @DeleteMapping
    public void deleteAllItemsByOwnerId(@Valid @NotBlank @RequestHeader(OWNER_HEADER) Long ownerId) {
        log.info("Получен DELETE-запрос к эндпоинту: '/items' на удаление всех items у пользователя с id {}", ownerId);
        itemService.deleteAllItemsByOwnerId(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsBySearchQuery(@RequestParam(required = false) String text) {
        log.info("Получен GET-запрос к эндпоинту: '/items/search' на получение списка всех items, содержащих подстроку {}", text);
        return itemService.getItemsBySearchQuery(text);
    }
}
