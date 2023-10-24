package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Transactional
class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    private final Sort sort = Sort.by(Sort.Order.asc("id"));
    private final User user = User.builder()
            .name("Вася")
            .email("vasya@yandex.ru")
            .build();
    private Item item = Item.builder()
            .name("Очки")
            .description("Солнцезащитные")
            .available(true)
            .owner(user)
            .itemRequest(null)
            .build();
    private Item item2 = Item.builder()
            .name("Кружка")
            .description("Кружка детская грибОчки")
            .available(true)
            .owner(user)
            .itemRequest(null)
            .build();
    private Item item3 = Item.builder()
            .name("Игрушка")
            .description("Собака плюшевая")
            .available(true)
            .owner(user)
            .itemRequest(null)
            .build();

    @Test
    public void shouldReturnTwoItemsBySearchQueryIfAvailableTrueAndPageable10() {
        userRepository.save(user);
        Item item1Expected = itemRepository.save(item);
        Item item2Expected = itemRepository.save(item2);
        itemRepository.save(item3);

        List<Item> items = itemRepository.getItemsBySearchQuery("чКи", PageRequest.of(0, 10, sort));
        assertEquals(2, items.size());
        assertEquals(item1Expected, items.get(0));
        assertEquals(item2Expected, items.get(1));
    }

    @Test
    public void shouldReturnEmptyListBySearchQueryIfNoItems() {
        List<Item> items = itemRepository.getItemsBySearchQuery("что-то", PageRequest.of(0, 10, sort));
        assertEquals(0, items.size());
    }

    @Test
    public void shouldReturnOneItemBySearchQueryIfAvailableTrueAndPageable1() {
        userRepository.save(user);
        Item item1Expected = itemRepository.save(item);
        itemRepository.save(item2);

        List<Item> items = itemRepository.getItemsBySearchQuery("чКи", PageRequest.of(0, 1, sort));
        assertEquals(1, items.size());
        assertEquals(item1Expected, items.get(0));
    }

    @Test
    public void shouldReturnEmptyListBySearchQueryIfNoAvailableItems() {
        userRepository.save(user);
        item.setAvailable(false);
        itemRepository.save(item);

        List<Item> items = itemRepository.getItemsBySearchQuery("чКи", PageRequest.of(0, 10, sort));
        assertEquals(0, items.size());
    }
}