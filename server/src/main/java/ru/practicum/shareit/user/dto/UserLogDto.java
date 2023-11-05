package ru.practicum.shareit.user.dto;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLogDto {
    private long id;
    private String name;
    private String email;
}
