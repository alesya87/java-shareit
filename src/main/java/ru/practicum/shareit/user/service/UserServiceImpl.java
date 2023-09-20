package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.User;
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
        log.info("сн=ервис - добавление пользователя");
        if (isDuplicateEmail(userDto)) {
            throw new DuplicateEmailException("Email " + userDto.getEmail() + " уже существует");
        }
        return UserMapper.mapToUserDto(userStorage.addUser(UserMapper.mapToUser(userDto)));
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long userId) {
        log.info("Сурвис -  обновление пользователя с id " + userId);
        getUserById(userId);
        userDto.setId(userId);
        if (userDto.getEmail() != null && isDuplicateEmail(userDto)) {
            throw new DuplicateEmailException("Email " + userDto.getEmail() + " уже существует");
        }
        return UserMapper.mapToUserDto(userStorage.updateUser(UserMapper.mapToUser(userDto)));
    }

    @Override
    public UserDto getUserById(Long id) {
        log.info("Сервис - получение пользователя по id " + id);
        User user = userStorage.getUserById(id);
        log.info("Проверка пользователя с id " + id + " на существование");
        if (user == null) {
            throw new EntityNotFoundException("пользователя с id " + id + " не существует");
        }
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Сервис - получение списка всех пользователей");
        return userStorage.getAllUsers().stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUserById(Long id) {
        log.info("Сервис - удаление пользователя по id " + id);
        userStorage.deleteUserById(id);
    }

    @Override
    public void deleteAllUsers() {
        log.info("Сервис - удаление всех пользователей");
        userStorage.deleteAllUsers();
    }

    private boolean isDuplicateEmail(UserDto userDto) {
        log.info("Проверка email на дубликат");
        return userStorage.getAllUsers()
                .stream()
                .filter(user -> user.getId() != userDto.getId())
                .map(User::getEmail)
                .anyMatch(mail -> mail.equals(userDto.getEmail()));
    }
}
