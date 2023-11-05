package ru.practicum.shareit.request.dto;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemRequestAddDto {
    private String description;
}
