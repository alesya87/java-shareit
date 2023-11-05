package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.comment.dto.CommentAddDto;
import ru.practicum.shareit.item.comment.dto.CommentInItemLogDto;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.ItemAddDto;
import ru.practicum.shareit.item.dto.ItemLogDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    @InjectMocks
    private ItemController controller;
    @MockBean
    private ItemService itemService;
    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldAddItem() throws Exception {
        long userId = 1L;
        ItemAddDto itemAddDto = new ItemAddDto("item name", "item description", true, null);
        ItemLogDto itemLogDto = new ItemLogDto(1L, itemAddDto.getName(), itemAddDto.getDescription(), true, userId,
                null, null, Collections.emptyList(), null);

        when(itemService.addItem(itemAddDto, userId)).thenReturn(itemLogDto);

        mockMvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemAddDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemLogDto.getName())))
                .andExpect(jsonPath("$.description", is(itemLogDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemLogDto.getAvailable())));

        verify(itemService, times(1)).addItem(itemAddDto, userId);
    }

    @Test
    public void shouldUpdateItem() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto(1L, "item name", "item description",
                true, null);
        ItemLogDto itemLogDto = new ItemLogDto(1L, itemUpdateDto.getName(), itemUpdateDto.getDescription(),
                true, userId,null, null, Collections.emptyList(), null);

        when(itemService.updateItem(itemUpdateDto, itemId, userId)).thenReturn(itemLogDto);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .content(mapper.writeValueAsString(itemUpdateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemLogDto.getName())))
                .andExpect(jsonPath("$.description", is(itemLogDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemLogDto.getAvailable())));

        verify(itemService, times(1)).updateItem(itemUpdateDto, itemId, userId);
    }

    @Test
    public void shouldReturnItemById() throws Exception {
        long itemId = 1L;
        long userId = 1L;
        ItemLogDto itemLogDto = new ItemLogDto(itemId, "item name", "item description",
                true, userId,null, null, Collections.emptyList(), null);

        when(itemService.getItemById(itemId, userId)).thenReturn(itemLogDto);

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemLogDto.getName())))
                .andExpect(jsonPath("$.description", is(itemLogDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemLogDto.getAvailable())));

        verify(itemService, times(1)).getItemById(itemId, userId);
    }

    @Test
    public void shouldReturnAllItemsByOwnerId() throws Exception {
        long userId = 1L;
        int from = 0;
        int size = 10;
        ItemLogDto itemLogDto1 = new ItemLogDto(1L, "item1 name", "item1 description",
                true, userId,null, null, Collections.emptyList(), null);
        ItemLogDto itemLogDto2 = new ItemLogDto(2L, "item2 name", "item2 description",
                true, userId,null, null, Collections.emptyList(), null);
        List<ItemLogDto> itemLogDtos = List.of(itemLogDto1, itemLogDto2);

        when(itemService.getAllItemsByOwnerId(userId, from, size)).thenReturn(itemLogDtos);

        mockMvc.perform(get("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(itemLogDto1.getName())))
                .andExpect(jsonPath("$[0].description", is(itemLogDto1.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemLogDto1.getAvailable())))
                .andExpect(jsonPath("$[1].name", is(itemLogDto2.getName())))
                .andExpect(jsonPath("$[1].description", is(itemLogDto2.getDescription())))
                .andExpect(jsonPath("$[1].available", is(itemLogDto2.getAvailable())));

        verify(itemService, times(1)).getAllItemsByOwnerId(userId, from, size);
    }

    @Test
    public void shouldDeleteItemById() throws Exception {
        doNothing().when(itemService).deleteItemById(1L);
        mockMvc.perform(delete("/items/{userId}", 1L))
                .andExpect(status().isOk());
        verify(itemService, times(1)).deleteItemById(1L);
    }

    @Test
    public void shouldReturnItemsBySearchQuery() throws Exception {
        String text = "name";
        long userId = 1L;
        int from = 0;
        int size = 10;
        ItemLogDto itemLogDto1 = new ItemLogDto(1L, "item1 name", "item1 description",
                true, userId,null, null, Collections.emptyList(), null);
        ItemLogDto itemLogDto2 = new ItemLogDto(2L, "item2 name", "item2 description",
                true, userId,null, null, Collections.emptyList(), null);
        List<ItemLogDto> itemLogDtos = List.of(itemLogDto1, itemLogDto2);

        when(itemService.getItemsBySearchQuery(text, from, size)).thenReturn(itemLogDtos);

        mockMvc.perform(get("/items/search")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("text", text)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(itemLogDto1.getName())))
                .andExpect(jsonPath("$[0].description", is(itemLogDto1.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemLogDto1.getAvailable())))
                .andExpect(jsonPath("$[1].name", is(itemLogDto2.getName())))
                .andExpect(jsonPath("$[1].description", is(itemLogDto2.getDescription())))
                .andExpect(jsonPath("$[1].available", is(itemLogDto2.getAvailable())));

        verify(itemService, times(1)).getItemsBySearchQuery(text, from, size);
    }

    @Test
    public void shouldAddComment() throws Exception {
        long itemId = 1L;
        long authorId = 1L;
        LocalDateTime created = LocalDateTime.now();
        CommentAddDto commentAddDto = new CommentAddDto("comment");
        CommentInItemLogDto commentInItemLogDto = new CommentInItemLogDto(1L, "comment", "Vasya", created);

        when(itemService.addComment(commentAddDto, authorId, itemId)).thenReturn(commentInItemLogDto);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .content(mapper.writeValueAsString(commentAddDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", authorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(commentInItemLogDto.getText())));

        verify(itemService, times(1)).addComment(commentAddDto, authorId, itemId);
    }
}
