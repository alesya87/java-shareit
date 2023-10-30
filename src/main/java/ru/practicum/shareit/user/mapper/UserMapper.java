package ru.practicum.shareit.user.mapper;

import ru.practicum.shareit.user.dto.UserLogDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserAddDto;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {
    public static User mapToUser(UserAddDto userAddDto) {
        return User.builder()
                .email(userAddDto.getEmail())
                .name(userAddDto.getName())
                .build();
    }

    public static UserLogDto mapToUserLogDto(User user) {
        return UserLogDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public static List<UserLogDto> mapToListUserLogDto(List<User> users) {
        return users.stream()
                .map(UserMapper::mapToUserLogDto)
                .collect(Collectors.toList());
    }
}
