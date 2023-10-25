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
    private  User userBeforeUpdate = User.builder()
            .id(1L)
            .email("userBeforeUpdate@email.com")
            .name("UserBeforeUpdate")
            .build();
    private UserUpdateDto userAfterUpdateName = UserUpdateDto.builder()
            .name("UserAfterUpdate")
            .build();

    private UserUpdateDto userAfterUpdateEmail = UserUpdateDto.builder()
            .email("userAfterUpdate@email.com")
            .build();

    @BeforeEach
    public void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    public void shouldUpdateUserName() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(userBeforeUpdate));
        when(userRepository.save(any()))
                .thenReturn(userBeforeUpdate);

        userService.updateUser(userAfterUpdateName, 1L);
        assertEquals(userBeforeUpdate.getName(), userAfterUpdateName.getName());
        verify(userRepository, times(1)).save(userBeforeUpdate);
    }

    @Test
    public void shouldUpdateUserEmail() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(userBeforeUpdate));
        when(userRepository.save(any()))
                .thenReturn(userBeforeUpdate);

        userService.updateUser(userAfterUpdateEmail, 1L);
        assertEquals(userBeforeUpdate.getEmail(), userAfterUpdateEmail.getEmail());
        verify(userRepository, times(1)).save(userBeforeUpdate);
    }

    @Test
    public void shouldTrowEntityNotFoundExceptionWhenUpdateIfUserNotExist() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.ofNullable(null));
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> userService.updateUser(userAfterUpdateEmail, 1L));
        assertEquals("пользователя с id 1 не существует", exception.getMessage());
        verify(userRepository, never()).save(Mockito.any(User.class));
    }
}