package ru.practicum.shareit.user.dto;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddDto {
    private String name;
    private String email;
}
