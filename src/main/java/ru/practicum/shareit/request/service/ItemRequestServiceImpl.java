package ru.practicum.shareit.request.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestAddDto;
import ru.practicum.shareit.request.dto.ItemRequestLogDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;


@Slf4j
@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private ItemRequestRepository itemRequestRepository;
    private UserRepository userRepository;

    public ItemRequestServiceImpl(ItemRequestRepository itemRequestRepository,
                                  UserRepository userRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ItemRequestLogDto addItemRequest(ItemRequestAddDto itemRequestAddDto, Long requesterId) {
        log.debug("Сервис - добавление запроса от пользователя {}", requesterId);
        User requester = getRequester(requesterId);
        ItemRequest itemRequest = itemRequestRepository.save(ItemRequestMapper.mapToItemRequest(itemRequestAddDto, requester));
        return ItemRequestMapper.mapToItemRequestLogDto(itemRequest);
    }

    @Override
    @Transactional
    public List<ItemRequestLogDto> getAllItemRequestsByUserId(Long requesterId) {
        log.debug("Сервис - проосомтр всех своих запросов от пользователя {}", requesterId);
        getRequester(requesterId);
        return ItemRequestMapper.mapToListItemRequestLogDto(itemRequestRepository
                .findByRequesterIdOrderByCreatedDesc(requesterId));
    }

    @Override
    @Transactional
    public List<ItemRequestLogDto> getAllItemRequests(Long requesterId, int from, int size) {
        log.debug("Сервис - проосомтр всех запросов от пользователя {}", requesterId);
        getRequester(requesterId);
        Sort sort = Sort.by(Sort.Order.desc("created"));
        Pageable pageable = PageRequest.of(from, size, sort);
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIdNot(requesterId, pageable);
        return ItemRequestMapper.mapToListItemRequestLogDto(itemRequests);
    }

    @Override
    @Transactional
    public ItemRequestLogDto getItemRequestById(Long userId, Long id) {
        log.debug("Сервис - проосомтр запроса с id {} от пользователя {}", id, userId);
        getRequester(userId);
        log.debug("Сервис - проверка запроса на существование");
        ItemRequest itemRequest = itemRequestRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Запроса с id " + id + " не существует"));
        return ItemRequestMapper.mapToItemRequestLogDto(itemRequest);
    }

    @Transactional
    private User getRequester(Long userId) {
        log.debug("Сервис - проверка пользователя на существование");
        User requester = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Пользователя с id " + userId + " не существует"));
        return requester;
    }
}
