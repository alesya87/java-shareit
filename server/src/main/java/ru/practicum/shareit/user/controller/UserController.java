package ru.practicum.shareit.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserAddDto;
import ru.practicum.shareit.user.dto.UserLogDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserLogDto addUser(@RequestBody UserAddDto userAddDto) {
        log.info("Получен POST-запрос к эндпоинту: '/users' на добавление пользователя: " +
                "name: |{}, email: {}", userAddDto.getName(), userAddDto.getEmail());
        return userService.addUser(userAddDto);
    }

    @PatchMapping("/{userId}")
    public UserLogDto updateUser(@RequestBody UserUpdateDto userUpdateDto, @PathVariable Long userId) {
        log.info("Получен PATCH-запрос к эндпоинту: '/users' на обновление пользователя с id {}: {}",
                userId, userUpdateDto.toString());
        return userService.updateUser(userUpdateDto, userId);
    }

    @GetMapping("/{userId}")
    public UserLogDto getUserById(@PathVariable Long userId) {
        log.info("Получен GET-запрос к эндпоинту: '/users/{userId}' на получение пользователя по id {}", userId);
        return userService.getUserById(userId);
    }

    @GetMapping
    public List<UserLogDto> getAllUsers() {
        log.info("Получен GET-запрос к эндпоинту: '/users' на получение списка всех пользователей");
        return userService.getAllUsers();
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Long userId) {
        log.info("Получен DELETE-запрос к эндпоинту: '/users/{userId}' на удаление пользователя по id {}", userId);
        userService.deleteUserById(userId);
    }
}
