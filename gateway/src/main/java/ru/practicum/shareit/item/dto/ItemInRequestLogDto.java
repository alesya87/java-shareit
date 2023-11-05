package ru.practicum.shareit.item.dto;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemInRequestLogDto {
    private Long id;
    private String name;
    private String description;
    private Long requestId;
    private Boolean available;
}
