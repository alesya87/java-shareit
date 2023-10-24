package ru.practicum.shareit.booking.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import ru.practicum.shareit.booking.model.BookingStatus;
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
