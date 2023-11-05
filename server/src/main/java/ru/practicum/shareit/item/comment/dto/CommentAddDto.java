package ru.practicum.shareit.item.comment.dto;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentAddDto {
    private String text;
}
