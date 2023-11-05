package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.item.dto.ItemLogDto;
import ru.practicum.shareit.user.dto.UserLogDto;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingLogDto {
    private Long id;
    private ItemLogDto item;
    private UserLogDto booker;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
}
