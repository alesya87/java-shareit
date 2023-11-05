package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserAddDto;
import ru.practicum.shareit.user.dto.UserLogDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserLogDto addUser(UserAddDto userAddDto) {
        log.debug("Сервис - добавление пользователя");
        return UserMapper.mapToUserLogDto(userRepository.save(UserMapper.mapToUser(userAddDto)));
    }

    @Override
    @Transactional
    public UserLogDto updateUser(UserUpdateDto userUpdateDto, Long userId) {
        log.debug("Сервис -  обновление пользователя с id {}", userId);
        User user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("пользователя с id " + userId + " не существует"));

        if (Objects.nonNull(userUpdateDto.getName())) {
            user.setName(userUpdateDto.getName());
        }
        if (Objects.nonNull(userUpdateDto.getEmail())) {
            user.setEmail(userUpdateDto.getEmail());
        }
        return UserMapper.mapToUserLogDto(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserLogDto getUserById(Long id) {
        log.debug("Сервис - получение пользователя по id {}", id);
        log.debug("Проверка пользователя с id {} на существование", id);
        User user = userRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("пользователя с id " + id + " не существует"));
        return UserMapper.mapToUserLogDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserLogDto> getAllUsers() {
        log.debug("Сервис - получение списка всех пользователей");
        return UserMapper.mapToListUserLogDto(userRepository.findAll());
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        log.debug("Сервис - удаление пользователя по id {}", id);
        userRepository.deleteById(id);
    }
}