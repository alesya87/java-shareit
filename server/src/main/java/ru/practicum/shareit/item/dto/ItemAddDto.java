package ru.practicum.shareit.item.dto;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemAddDto {
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
}
