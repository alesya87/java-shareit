package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        log.debug("Сервис - добавление пользователя");
        if (isDuplicateEmail(userDto, userDto.getId())) {
            throw new DuplicateEmailException("Email " + userDto.getEmail() + " уже существует");
        }
        return userStorage.addUser(userDto);
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long userId) {
        log.debug("Сервис -  обновление пользователя с id {}", userId);
        getUserById(userId);
        if (userDto.getEmail() != null && isDuplicateEmail(userDto, userId)) {
            throw new DuplicateEmailException("Email " + userDto.getEmail() + " уже существует");
        }
        return userStorage.updateUser(userDto, userId);
    }

    @Override
    public UserDto getUserById(Long id) {
        log.debug("Сервис - получение пользователя по id {}", id);
        UserDto userDto = userStorage.getUserById(id);
        log.debug("Проверка пользователя с id {} на существование", id);
        if (userDto == null) {
            throw new EntityNotFoundException("пользователя с id " + id + " не существует");
        }
        return userDto;
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.debug("Сервис - получение списка всех пользователей");
        return userStorage.getAllUsers();
    }

    @Override
    public void deleteUserById(Long id) {
        log.debug("Сервис - удаление пользователя по id {}", id);
        userStorage.deleteUserById(id);
    }

    @Override
    public void deleteAllUsers() {
        log.debug("Сервис - удаление всех пользователей");
        userStorage.deleteAllUsers();
    }

    private boolean isDuplicateEmail(UserDto userDto, Long userId) {
        log.debug("Проверка email на дубликат");
        return userStorage.getAllUsers()
                .stream()
                .filter(userStorage -> userStorage.getId() != userId)
                .map(UserDto::getEmail)
                .anyMatch(mail -> mail.equals(userDto.getEmail()));
    }
}
