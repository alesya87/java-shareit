package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserAddDto;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserLogDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Component
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserLogDto addUser(UserAddDto userAddDto) {
        log.debug("Сервис - добавление пользователя");
        return UserMapper.mapToUserLogDto(userRepository.save(UserMapper.mapToUser(userAddDto)));
    }

    @Override
    public UserLogDto updateUser(UserUpdateDto userUpdateDto, Long userId) {
        log.debug("Сервис -  обновление пользователя с id {}", userId);
        UserLogDto userBeforeUpdate = getUserById(userId);
        if (userUpdateDto.getName() == null) {
            userUpdateDto.setName(userBeforeUpdate.getName());
        }
        if (userUpdateDto.getEmail() == null) {
            userUpdateDto.setEmail(userBeforeUpdate.getEmail());
        }
        return UserMapper.mapToUserLogDto(userRepository.save(UserMapper.mapToUser(userUpdateDto, userId)));
    }

    @Override
    public UserLogDto getUserById(Long id) {
        log.debug("Сервис - получение пользователя по id {}", id);
        log.debug("Проверка пользователя с id {} на существование", id);
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new EntityNotFoundException("пользователя с id " + id + " не существует");
        }
        return UserMapper.mapToUserLogDto(user);
    }

    @Override
    public List<UserLogDto> getAllUsers() {
        log.debug("Сервис - получение списка всех пользователей");
        return UserMapper.mapToListUserLogDto(userRepository.findAll());
    }

    @Override
    public void deleteUserById(Long id) {
        log.debug("Сервис - удаление пользователя по id {}", id);
        userRepository.deleteById(id);
    }
}