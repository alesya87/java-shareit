package ru.practicum.shareit.item.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.comment.dto.CommentAddDto;
import ru.practicum.shareit.item.dto.ItemAddDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> addItem(ItemAddDto itemAddDto, Long ownerId) {
        return post("", ownerId, itemAddDto);
    }

    public ResponseEntity<Object> updateItem(ItemUpdateDto itemUpdateDto, Long itemId, Long ownerId) {
        return patch("/" + itemId, ownerId, itemUpdateDto);
    }

    public ResponseEntity<Object> getItemById(Long itemId, Long ownerId) {
        return get("/" + itemId, ownerId);
    }

    public ResponseEntity<Object> getAllItemsByOwnerId(Long ownerId, Integer from, Integer size) {
        String path = "?from=" + from + "&size=" + size;
        return get(path, ownerId, null);
    }

    public ResponseEntity<Object> deleteItemById(Long itemId) {
        return delete("/" + itemId);
    }

    public ResponseEntity<Object> getItemsBySearchQuery(String text, Integer from, Integer size) {
        String path = "/search?text=" + text + "&from=" + from + "&size=" + size;
        return get(path, null, null);
    }

    public ResponseEntity<Object> addComment(CommentAddDto commentAddDto, Long authorId, Long itemId) {
        return post("/" + itemId + "/comment", authorId, commentAddDto);
    }
}
