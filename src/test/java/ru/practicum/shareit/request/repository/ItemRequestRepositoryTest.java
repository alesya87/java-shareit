package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRequestRepositoryTest {
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldReturnRequestByRequesterId() {
        User user = new User(null,"user1 name", "user1 description");
        User userAdded = userRepository.save(user);

        ItemRequest request1 = new ItemRequest(null, "request", userAdded, LocalDateTime.now(), Collections.emptyList());
        ItemRequest requestAdded1 = itemRequestRepository.save(request1);

        ItemRequest request2 = new ItemRequest(null, "request2", userAdded, LocalDateTime.now(), Collections.emptyList());
        ItemRequest requestAdded2 = itemRequestRepository.save(request2);

        List<ItemRequest> requests = itemRequestRepository.findByRequesterIdOrderByCreatedDesc(userAdded.getId());

        assertEquals(2, requests.size());
        assertEquals(requestAdded2, requests.get(0));
        assertEquals(requestAdded1, requests.get(1));
    }

    @Test
    void shouldReturnRequestByRequesterIdNot() {
        Sort sort = Sort.by(Sort.Order.desc("created"));
        Pageable pageable = PageRequest.of(0 / 10, 10, sort);

        User user1 = new User(null,"user1 name", "user1 description");
        User userAdded1 = userRepository.save(user1);

        User user2 = new User(null,"user2 name", "user2 description");
        User userAdded2 = userRepository.save(user2);

        ItemRequest request1 = new ItemRequest(null, "request", userAdded1, LocalDateTime.now(), Collections.emptyList());
        ItemRequest requestAdded1 = itemRequestRepository.save(request1);

        ItemRequest request2 = new ItemRequest(null, "request2", userAdded2, LocalDateTime.now(), Collections.emptyList());
        ItemRequest requestAdded2 = itemRequestRepository.save(request2);

        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterIdNot(userAdded1.getId(), pageable);

        assertEquals(1, requests.size());
        assertEquals(requestAdded2, requests.get(0));
    }
}