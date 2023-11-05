package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserAddDto;
import ru.practicum.shareit.user.dto.UserLogDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    @InjectMocks
    private UserController controller;
    @MockBean
    private UserService userService;
    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldAddUser() throws Exception {
        UserAddDto userAddDto = new UserAddDto("Вася", "asdfgh@gmail.com");
        UserLogDto userLogDto = new UserLogDto();
        userLogDto.setId(1);
        userLogDto.setName(userAddDto.getName());
        userLogDto.setEmail(userAddDto.getEmail());

        when(userService.addUser(userAddDto))
                .thenAnswer(invocationOnMock -> {
                    invocationOnMock.getArgument(0, UserAddDto.class);
                    return userLogDto;
                });

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userAddDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(userLogDto.getName())))
                .andExpect(jsonPath("$.email", is(userLogDto.getEmail())));

        verify(userService, times(1)).addUser(userAddDto);
    }

    @Test
    public void shouldUpdateUser() throws Exception {
        UserUpdateDto userUpdateDto = new UserUpdateDto("Вася", "vasya@gmail.com");
        UserLogDto userLogDto = new UserLogDto();
        userLogDto.setId(1);
        userLogDto.setName(userUpdateDto.getName());
        userLogDto.setEmail(userUpdateDto.getEmail());

        when(userService.updateUser(userUpdateDto, 1L))
                .thenAnswer(invocationOnMock -> {
                    invocationOnMock.getArgument(0, UserUpdateDto.class);
                    return userLogDto;
                });

        mockMvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(userUpdateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(userLogDto.getName())))
                .andExpect(jsonPath("$.email", is(userLogDto.getEmail())));

        verify(userService, times(1)).updateUser(userUpdateDto, 1L);
    }

    @Test
    public void shouldThrowExceptionWhenUpdateUserIfUserIdNull() throws Exception {
        mockMvc.perform(patch("/users/")
                        .content(mapper.writeValueAsString(new UserUpdateDto()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(userService, never()).updateUser(any(UserUpdateDto.class), anyLong());
    }

    @Test
    public void shouldReturnUserById() throws Exception {
        UserLogDto userLogDto = new UserLogDto(1L, "Вася", "asdfgh@gmail.com");

        when(userService.getUserById(1L)).thenReturn(userLogDto);

        mockMvc.perform(get("/users/{userId}", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(userLogDto.getName())))
                .andExpect(jsonPath("$.email", is(userLogDto.getEmail())));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    public void shouldReturnAllUsers() throws Exception {
        UserLogDto userLogDto1 = new UserLogDto(1L, "Вася", "vasya@gmail.com");
        UserLogDto userLogDto2 = new UserLogDto(2L, "Юра", "ura@gmail.com");
        List<UserLogDto> userLogDtos = List.of(userLogDto1, userLogDto2);

        when(userService.getAllUsers()).thenReturn(userLogDtos);

        mockMvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(userLogDtos.get(0).getName())))
                .andExpect(jsonPath("$[0].email", is(userLogDtos.get(0).getEmail())))
                .andExpect(jsonPath("$[1].name", is(userLogDtos.get(1).getName())))
                .andExpect(jsonPath("$[1].email", is(userLogDtos.get(1).getEmail())));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    public void shouldDeleteUserById() throws Exception {
        doNothing().when(userService).deleteUserById(1L);
        mockMvc.perform(delete("/users/{userId}", 1L))
                .andExpect(status().isOk());
        verify(userService, times(1)).deleteUserById(1L);
    }
}
