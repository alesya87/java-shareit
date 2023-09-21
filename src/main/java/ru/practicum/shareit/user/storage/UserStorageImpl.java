package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UserStorageImpl implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long id = 0;

    @Override
    public UserDto addUser(UserDto userDto) {
        log.debug("Добавление пользователя в хранилище");
        User user = UserMapper.mapToUser(userDto);
        user.setId(++id);
        users.put(user.getId(), user);
        log.debug("Пользователь с id {} добавлен в хранилище", user.getId());
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long userId) {
        log.debug("Обновление пользователя с id {} в хранилище", userDto.getId());
        User user = users.get(userId);
        user.setName(userDto.getName() != null ? userDto.getName() : user.getName());
        user.setEmail(userDto.getEmail() != null ? userDto.getEmail() : user.getEmail());
        users.put(userId, user);
        log.debug("Пользователь с id {} обновлен в хранилище", userId);
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public UserDto getUserById(Long id) {
        log.debug("Получение пользователя по id {} из хранилища", id);
        User user = users.get(id);
        return user != null ? UserMapper.mapToUserDto(user) : null;
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.debug("Получение списка всех пользователей из хранилища");
        return new ArrayList<>(users.values()).stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUserById(Long id) {
        log.debug("Удаление пользователя по id {} из хранилища", id);
        users.remove(id);
    }

    @Override
    public void deleteAllUsers() {
        log.debug("Удаление всех полльзователей из хранилища");
        users.clear();
    }
}
