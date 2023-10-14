package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentAddDto;
import ru.practicum.shareit.item.comment.dto.CommentInItemLogDto;
import ru.practicum.shareit.item.dto.ItemAddDto;
import ru.practicum.shareit.item.dto.ItemLogDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.List;

public interface ItemService {
    ItemLogDto addItem(ItemAddDto itemAddDto, Long ownerId);

    ItemLogDto updateItem(ItemUpdateDto itemUpdateDto, Long itemId, Long ownerId);

    ItemLogDto getItemById(Long itemId, Long ownerId);

    List<ItemLogDto> getAllItemsByOwnerId(Long ownerId);

    void deleteItemById(Long itemId);

    List<ItemLogDto> getItemsBySearchQuery(String text);

    CommentInItemLogDto addComment(CommentAddDto comment, Long authorId, Long itemId);
}
