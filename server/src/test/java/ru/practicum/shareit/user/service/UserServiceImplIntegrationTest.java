package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserAddDto;
import ru.practicum.shareit.user.dto.UserLogDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserServiceImplIntegrationTest {
    @Autowired
    private UserService userService;

    @Test
    public void integrationTest() {
        UserAddDto userAddDto1 = new UserAddDto("user1 name", "user1@email.com");
        UserLogDto userLogDto1 = userService.addUser(userAddDto1);
        UserAddDto userAddDto2 = new UserAddDto("user2 name", "user2@email.com");
        UserLogDto userLogDto2 = userService.addUser(userAddDto2);

        assertEquals(userLogDto1, userService.getUserById(userLogDto1.getId()));
        assertEquals(userLogDto2, userService.getUserById(userLogDto2.getId()));
        assertEquals(2, userService.getAllUsers().size());

        userService.deleteUserById(userLogDto1.getId());
        assertEquals(1, userService.getAllUsers().size());

        UserUpdateDto userUpdateDto = new UserUpdateDto("userUpdateDto name", "userUpdateDto@email.com");
        UserLogDto userLogDtoAfterUpdate = userService.updateUser(userUpdateDto, userLogDto2.getId());

        assertEquals(userUpdateDto.getEmail(), userLogDtoAfterUpdate.getEmail());
        assertEquals(userUpdateDto.getName(), userLogDtoAfterUpdate.getName());

        userService.deleteUserById(userLogDto2.getId());
        assertEquals(0, userService.getAllUsers().size());

        Exception exception1 = assertThrows(EntityNotFoundException.class,
                () ->  userService.getUserById(150L));
        assertEquals("пользователя с id 150 не существует", exception1.getMessage());
    }
}