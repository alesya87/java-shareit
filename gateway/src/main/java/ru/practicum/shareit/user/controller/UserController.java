package ru.practicum.shareit.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserAddDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import javax.validation.Valid;


@Slf4j
@RestController
@RequestMapping(path = "/users")
@Validated
public class UserController {
    private final UserClient userClient;

    public UserController(UserClient userClient) {
        this.userClient = userClient;
    }

    @PostMapping
    public ResponseEntity<Object> addUser(@Valid @RequestBody UserAddDto userAddDto) {
        log.info("Получен POST-запрос к эндпоинту: '/users' на добавление пользователя: " +
                "name: |{}, email: {}", userAddDto.getName(), userAddDto.getEmail());
        return userClient.addUser(userAddDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@Valid @RequestBody UserUpdateDto userUpdateDto, @PathVariable Long userId) {
        log.info("Получен PATCH-запрос к эндпоинту: '/users' на обновление пользователя с id {}: {}",
                userId, userUpdateDto.toString());
        return userClient.updateUser(userUpdateDto, userId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(@PathVariable Long userId) {
        log.info("Получен GET-запрос к эндпоинту: '/users/{userId}' на получение пользователя по id {}", userId);
        return userClient.getUserById(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("Получен GET-запрос к эндпоинту: '/users' на получение списка всех пользователей");
        return userClient.getAllUsers();
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Long userId) {
        log.info("Получен DELETE-запрос к эндпоинту: '/users/{userId}' на удаление пользователя по id {}", userId);
        userClient.deleteUserById(userId);
    }
}
