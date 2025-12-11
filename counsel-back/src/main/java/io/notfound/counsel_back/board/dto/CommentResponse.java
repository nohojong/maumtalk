package io.notfound.counsel_back.board.dto;

import io.notfound.counsel_back.board.entity.Comment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentResponse {
    private Long id;
    private Long postId;
    private String writerEmail; // 이름 대신 이메일 필드로 수정
    private String content;
    private final boolean isBlinded;
    private LocalDateTime createdAt;

    // 정적 팩토리 메서드
    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .writerEmail(comment.getWriter().getEmail())
                .content(comment.getContent())
                .isBlinded(comment.isBlinded())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
