package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.IncorrectEmailException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
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
    public UserDto addUser(@RequestBody @Valid UserDto userDto) {
        log.info("Получен POST-запрос к эндпоинту: '/users' на добавление пользователя: " +
                "name: " + userDto.getName() + ", email: " + userDto.getEmail());
        if (!isValidEmail(userDto.getEmail())) {
            throw new IncorrectEmailException("Некорректный email: " + userDto.getEmail());
        }
        return userService.addUser(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@RequestBody UserDto userDto, @Valid @PathVariable @NotBlank Long userId) {
        log.info("Получен PATCH-запрос к эндпоинту: '/users' на обновление пользователя с id " + userId +
                ": " + userDto.toString());
        if (userDto.getEmail() != null && !isValidEmail(userDto.getEmail())) {
            throw new IncorrectEmailException("Некорректный email: " + userDto.getEmail());
        }
        return userService.updateUser(userDto, userId);
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@Valid @PathVariable @NotBlank Long userId) {
        log.info("Получен GET-запрос к эндпоинту: '/users/{userId}' на получение пользователя по id " + userId);
        return userService.getUserById(userId);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("Получен GET-запрос к эндпоинту: '/users' на получение списка всех пользователей");
        return userService.getAllUsers();
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Long userId) {
        log.info("Получен DELETE-запрос к эндпоинту: '/users/{userId}' на удаление пользователя по id " + userId);
        userService.deleteUserById(userId);
    }

    @DeleteMapping
    public void deleteAllUsers() {
        log.info("Получен DELETE-запрос к эндпоинту: '/users' на удаление всех пользователей");
        userService.deleteAllUsers();
    }

    private boolean isValidEmail(String email) {
        log.info("Проверка email на корректность");
        return email.contains("@");
    }
}
