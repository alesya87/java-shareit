package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestAddDto;
import ru.practicum.shareit.request.dto.ItemRequestLogDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    private ItemRequestService itemRequestService;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        itemRequestService = new ItemRequestServiceImpl(itemRequestRepository, userRepository);
    }

    @Test
    public void shouldAddItemRequestIfUserExist() {
        long requesterId = 1L;
        User requester = new User(requesterId, "user1", "user1@email.com");
        ItemRequestAddDto itemRequestAddDto = new ItemRequestAddDto("itemRequest1 description");
        ItemRequest itemRequest = new ItemRequest(1L, "itemRequest1 description", requester,
                LocalDateTime.of(2023, 10, 25, 22, 23), Collections.emptyList());

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestLogDto itemRequestLogDto = itemRequestService.addItemRequest(itemRequestAddDto, requesterId);

        assertNotNull(itemRequestLogDto);

        verify(userRepository, times(1)).findById(requesterId);
        verify(itemRequestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionWhenItemRequesterNotExist() {
        long requesterId = 1L;
        ItemRequestAddDto itemRequestAddDto = new ItemRequestAddDto("itemRequest1 description");

        when(userRepository.findById(requesterId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                itemRequestService.addItemRequest(itemRequestAddDto, requesterId));

        assertEquals("Пользователя с id 1 не существует", exception.getMessage());

        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    public void shouldReturnItemRequestIfItemExist() {
        long requesterId = 1L;
        User requester = new User(requesterId, "user1", "user1@email.com");
        ItemRequest itemRequest = new ItemRequest(1L, "itemRequest1 description", requester,
                LocalDateTime.of(2023, 10, 25, 22, 23), Collections.emptyList());

        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findById(requesterId)).thenReturn(Optional.of(itemRequest));

        ItemRequestLogDto itemRequestLogDto = itemRequestService.getItemRequestById(requesterId, 1L);

        assertNotNull(itemRequestLogDto);

        verify(userRepository, times(1)).findById(requesterId);
        verify(itemRequestRepository, times(1)).findById(1L);
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionWhenItemNotExist() {
        long requesterId = 1L;
        User requester = new User(requesterId, "user1", "user1@email.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                itemRequestService.getItemRequestById(requesterId, 1L));

        assertEquals("Запроса с id 1 не существует", exception.getMessage());

        verify(userRepository, times(1)).findById(requesterId);
        verify(itemRequestRepository, times(1)).findById(1L);
    }
}