package ru.practicum.shareit.request.controller;

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
import ru.practicum.shareit.request.dto.ItemRequestAddDto;
import ru.practicum.shareit.request.dto.ItemRequestLogDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserAddDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
}
