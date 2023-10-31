package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestAddDto;
import ru.practicum.shareit.request.dto.ItemRequestLogDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    @InjectMocks
    private ItemRequestController controller;
    @MockBean
    private ItemRequestService itemRequestService;
    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldAddItemRequest() throws Exception {
        ItemRequestAddDto itemRequestAddDto = new ItemRequestAddDto("description");
        ItemRequestLogDto itemRequestLogDto = new ItemRequestLogDto(1L, itemRequestAddDto.getDescription(),
                LocalDateTime.now(), Collections.emptyList());

        when(itemRequestService.addItemRequest(itemRequestAddDto, 1L))
                .thenReturn(itemRequestLogDto);

        mockMvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestAddDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is(itemRequestLogDto.getDescription())));

        verify(itemRequestService, times(1)).addItemRequest(itemRequestAddDto, 1L);
    }

    @Test
    public void shouldThrowExceptionWhenAddItemRequestIfNoUserHeader() throws Exception {
        ItemRequestAddDto itemRequestAddDto = new ItemRequestAddDto("description");
        Matcher<String> contentMatcher = CoreMatchers
                .containsString("X-Sharer-User-Id");

        mockMvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestAddDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(contentMatcher));

        verify(itemRequestService, never()).addItemRequest(any(ItemRequestAddDto.class), anyLong());
    }

    @Test
    public void shouldThrowExceptionWhenAddItemRequestIfDescriptionEmpty() throws Exception {
        ItemRequestAddDto itemRequestAddDto = new ItemRequestAddDto("  ");
        Matcher<String> contentMatcher = CoreMatchers
                .containsString("default message [description]]; default message [must not be blank]]");

        mockMvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestAddDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(contentMatcher));

        verify(itemRequestService, never()).addItemRequest(any(ItemRequestAddDto.class), anyLong());
    }

    @Test
    public void shouldReturnItemRequestsByUserId() throws Exception {
        ItemRequestLogDto itemRequestLogDto1 = new ItemRequestLogDto(1L, "description 1",
                LocalDateTime.now(), Collections.emptyList());
        ItemRequestLogDto itemRequestLogDto2 = new ItemRequestLogDto(2L, "description 2",
                LocalDateTime.now(), Collections.emptyList());
        List<ItemRequestLogDto> itemRequestLogDtos = List.of(itemRequestLogDto1, itemRequestLogDto2);

        when(itemRequestService.getAllItemRequestsByUserId(1L))
                .thenReturn(itemRequestLogDtos);

        mockMvc.perform(get("/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description", is(itemRequestLogDto1.getDescription())))
                .andExpect(jsonPath("$[1].description", is(itemRequestLogDto2.getDescription())));

        verify(itemRequestService, times(1)).getAllItemRequestsByUserId(1L);
    }

    @Test
    public void shouldThrowExceptionWhenGetAllItemRequestsByUserIdIfNoUserHeader() throws Exception {
        Matcher<String> contentMatcher = CoreMatchers
                .containsString("X-Sharer-User-Id");

        mockMvc.perform(get("/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(contentMatcher));

        verify(itemRequestService, never()).getAllItemRequestsByUserId(anyLong());
    }

    @Test
    public void shouldReturnAllItemRequests() throws Exception {
        ItemRequestLogDto itemRequestLogDto1 = new ItemRequestLogDto(1L, "description 1",
                LocalDateTime.now(), Collections.emptyList());
        ItemRequestLogDto itemRequestLogDto2 = new ItemRequestLogDto(2L, "description 2",
                LocalDateTime.now(), Collections.emptyList());
        List<ItemRequestLogDto> itemRequestLogDtos = List.of(itemRequestLogDto1, itemRequestLogDto2);

        when(itemRequestService.getAllItemRequests(1L, 0, 10)).thenReturn(itemRequestLogDtos);

        mockMvc.perform(get("/requests/all")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description", is(itemRequestLogDto1.getDescription())))
                .andExpect(jsonPath("$[1].description", is(itemRequestLogDto2.getDescription())));

        verify(itemRequestService, times(1)).getAllItemRequests(1L, 0, 10);
    }

    @Test
    public void shouldThrowExceptionWhenGetAllItemRequestsIfFromNegative() throws Exception {
        Matcher<String> contentMatcher = CoreMatchers
                .containsString("from: must be greater than or equal to 0");

        mockMvc.perform(get("/requests/all")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(contentMatcher));

        verify(itemRequestService, never()).getAllItemRequests(anyLong(), anyInt(), anyInt());
    }

    @Test
    public void shouldThrowExceptionWhenGetAllItemRequestsIfSize0() throws Exception {
        Matcher<String> contentMatcher = CoreMatchers
                .containsString("size: must be greater than or equal to 1");

        mockMvc.perform(get("/requests/all")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(contentMatcher));

        verify(itemRequestService, never()).getAllItemRequests(anyLong(), anyInt(), anyInt());
    }

    @Test
    public void shouldReturnItemRequestById() throws Exception {
        ItemRequestLogDto itemRequestLogDto = new ItemRequestLogDto(1L, "description 1",
                LocalDateTime.now(), Collections.emptyList());

        when(itemRequestService.getItemRequestById(1L, 1L)).thenReturn(itemRequestLogDto);

        mockMvc.perform(get("/requests/{requestId}", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is(itemRequestLogDto.getDescription())));

        verify(itemRequestService, times(1)).getItemRequestById(1L, 1L);
    }

    @Test
    public void shouldThrowExceptionWhenGetItemRequestByIdIfNoUserHeader() throws Exception {
        Matcher<String> contentMatcher = CoreMatchers
                .containsString("X-Sharer-User-Id");

        mockMvc.perform(get("/requests/{requestId}", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(contentMatcher));

        verify(itemRequestService, never()).getItemRequestById(anyLong(), anyLong());
    }
}
