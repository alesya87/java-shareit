package ru.practicum.shareit.item.comment.mapper;

import ru.practicum.shareit.item.comment.dto.CommentAddDto;
import ru.practicum.shareit.item.comment.dto.CommentInItemLogDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommentMapper {
    public static CommentInItemLogDto mapToCommentInItemLogDto(Comment comment) {
        return CommentInItemLogDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    public static Comment mapToComment(CommentAddDto commentAddDto, Long itemId, User author) {
        return Comment.builder()
                .text(commentAddDto.getText())
                .itemId(itemId)
                .author(author)
                .created(LocalDateTime.now())
                .build();
    }

        public static List<CommentInItemLogDto> mapToListCommentInItemLogDto(List<Comment> comments) {
        return comments.stream()
                .map(CommentMapper::mapToCommentInItemLogDto)
                .collect(Collectors.toList());
    }
}
