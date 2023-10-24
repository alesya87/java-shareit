package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserAddDto;
import ru.practicum.shareit.user.dto.UserLogDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

public interface UserService {
    UserLogDto addUser(UserAddDto userAddDto);

    UserLogDto updateUser(UserUpdateDto userUpdateDto, Long userId);

    UserLogDto getUserById(Long id);

    List<UserLogDto> getAllUsers();

    void deleteUserById(Long id);
}
