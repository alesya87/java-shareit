package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestAddDto;
import ru.practicum.shareit.request.dto.ItemRequestLogDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.List;

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

    @Test
    public void shouldGetAllItemRequests() {
        long requesterId = 1L;
        User requester = new User(requesterId, "user1", "user1@email.com");
        ItemRequest itemRequest1 = new ItemRequest(1L, "itemRequest1 description", requester,
                LocalDateTime.of(2023, 10, 25, 22, 23), Collections.emptyList());
        ItemRequest itemRequest2 = new ItemRequest(2L, "itemRequest2 description", requester,
                LocalDateTime.of(2023, 10, 28, 22, 23), Collections.emptyList());
        List<ItemRequest> itemRequestDtos = List.of(itemRequest1, itemRequest2);

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findAllByRequesterIdNot(anyLong(), any(Pageable.class)))
                .thenReturn(itemRequestDtos);

        List<ItemRequestLogDto> result = itemRequestService.getAllItemRequests(requesterId, 0, 10);

        assertNotNull(result);
        assertEquals(ItemRequestMapper.mapToListItemRequestLogDto(itemRequestDtos), result);

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findAllByRequesterIdNot(anyLong(), any(Pageable.class));
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionWhenGetAllItemRequestsIfUserNotExist() {
        long requesterId = 1L;

        when(userRepository.findById(requesterId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                itemRequestService.getItemRequestById(requesterId, 1L));

        assertEquals("Пользователя с id 1 не существует", exception.getMessage());

        verify(userRepository, times(1)).findById(requesterId);
        verify(itemRequestRepository, never()).findAllByRequesterIdNot(anyLong(), any(Pageable.class));
    }

    @Test
    public void shouldGetAllItemRequestsByUserId() {
        long requesterId = 1L;
        User requester = new User(requesterId, "user1", "user1@email.com");
        ItemRequest itemRequest1 = new ItemRequest(1L, "itemRequest1 description", requester,
                LocalDateTime.of(2023, 10, 25, 22, 23), Collections.emptyList());
        ItemRequest itemRequest2 = new ItemRequest(2L, "itemRequest2 description", requester,
                LocalDateTime.of(2023, 10, 28, 22, 23), Collections.emptyList());
        List<ItemRequest> itemRequestDtos = List.of(itemRequest1, itemRequest2);

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findByRequesterIdOrderByCreatedDesc(requesterId))
                .thenReturn(itemRequestDtos);

        List<ItemRequestLogDto> result = itemRequestService.getAllItemRequestsByUserId(requesterId);

        assertNotNull(result);
        assertEquals(ItemRequestMapper.mapToListItemRequestLogDto(itemRequestDtos), result);

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findByRequesterIdOrderByCreatedDesc(requesterId);
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionWhenGetAllItemRequestsByUserIdIfUserNotExist() {
        long requesterId = 1L;

        when(userRepository.findById(requesterId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                itemRequestService.getAllItemRequestsByUserId(requesterId));

        assertEquals("Пользователя с id 1 не существует", exception.getMessage());

        verify(userRepository, times(1)).findById(requesterId);
        verify(itemRequestRepository, never()).findByRequesterIdOrderByCreatedDesc(anyLong());
    }
}