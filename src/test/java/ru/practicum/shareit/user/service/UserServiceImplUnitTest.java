package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplUnitTest {
    private UserService userService;
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    public void shouldUpdateUserName() {
        long userId = 1L;
        User user = new User(userId, "UserBeforeUpdate", "userBeforeUpdate@email.com");
        UserUpdateDto userUpdateName = new UserUpdateDto("UserAfterUpdate", null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        assertEquals("UserBeforeUpdate", user.getName());

        userService.updateUser(userUpdateName, userId);

        assertEquals("UserAfterUpdate", user.getName());
        assertEquals("userBeforeUpdate@email.com", user.getEmail());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void shouldUpdateUserEmail() {
        long userId = 1L;
        User user = new User(userId, "UserBeforeUpdate", "userBeforeUpdate@email.com");
        UserUpdateDto userUpdateEmail = new UserUpdateDto(null, "userAfterUpdate@email.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        assertEquals("userBeforeUpdate@email.com", user.getEmail());

        userService.updateUser(userUpdateEmail, userId);

        assertEquals("userAfterUpdate@email.com", user.getEmail());
        assertEquals("UserBeforeUpdate", user.getName());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void shouldTrowEntityNotFoundExceptionWhenGetUserByIdNotExist() {
        long userId = 1L;
        UserUpdateDto userUpdateEmail = new UserUpdateDto(null, "userAfterUpdate@email.com");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class, () -> userService.updateUser(userUpdateEmail, 1L));

        assertEquals("пользователя с id 1 не существует", exception.getMessage());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(Mockito.any(User.class));
    }
}