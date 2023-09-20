package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class UserStorageImpl implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long id = 0;

    @Override
    public User addUser(User user) {
        log.info("Добавление пользователя в хранилище");
        users.put(++id, user);
        user.setId(id);
        log.info("Пользователь с id " + id + " добавлен в хранилище");
        return user;
    }

    @Override
    public User updateUser(User user) {
        log.info("Обновление пользователя с id " + user.getId() + " в хранилище");
        User userBeforeUpdate = users.get(user.getId());
        user.setName(user.getName() != null ? user.getName() : userBeforeUpdate.getName());
        user.setEmail(user.getEmail() != null ? user.getEmail() : userBeforeUpdate.getEmail());
        users.put(user.getId(), user);
        log.info("Пользователь с id " + user.getId() + " обновлен в хранилище");
        return user;
    }

    @Override
    public User getUserById(Long id) {
        log.info("Получение пользователя по id " + id + " из хранилища");
        return users.get(id);
    }

    @Override
    public List<User> getAllUsers() {
        log.info("Получение списка всех пользователей из хранилища");
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteUserById(Long id) {
        log.info("Удаление пользователя по id " + id + " из хранилища");
        users.remove(id);
    }

    @Override
    public void deleteAllUsers() {
        log.info("Удаление всех полльзователей из хранилища");
        users.clear();
    }
}
