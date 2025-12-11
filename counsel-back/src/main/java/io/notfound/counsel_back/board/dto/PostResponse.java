package io.notfound.counsel_back.board.dto;

import io.notfound.counsel_back.board.entity.Attachment;
import io.notfound.counsel_back.board.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Builder
public class PostResponse {

    private Long postId;
    private Long authorId;
    private String title;
    private String content;
    private List<String> attachmentUrls;
    private Integer viewCount;
    private Integer commentCount;
    private Integer likeCount;   // 좋아요 수
    private Boolean liked;       // 로그인 유저의 좋아요 여부
    private String createdAt;

    // 기본적으로 liked는 false로 설정
    public static PostResponse from(Post post) {
        return from(post, false);
    }

    // 로그인한 사용자의 좋아요 여부도 포함
    public static PostResponse from(Post post, boolean liked) {
        return PostResponse.builder()
                .postId(post.getId())
                .authorId(post.getAuthor() != null ? post.getAuthor().getId() : null)
                .title(post.getTitle())
                .content(post.getContent())
                .attachmentUrls(post.getAttachments() != null
                        ? post.getAttachments().stream().map(Attachment::getFileUrl).toList()
                        : List.of())
                .viewCount(post.getViews())
                .commentCount(post.getCommentCount())
                .likeCount(post.getLikeCount())
                .liked(liked)
                .createdAt(post.getCreatedAt() != null
                        ? post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        : null)
                .build();
    }
}
