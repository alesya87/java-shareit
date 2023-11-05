package ru.practicum.shareit.user.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddDto {
    @NotBlank
    private String name;
    @NotBlank
    @Email(regexp = "^(.+)@(\\S+)$")
    private String email;
}
