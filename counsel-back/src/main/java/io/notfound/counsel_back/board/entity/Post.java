package io.notfound.counsel_back.board.entity;

import io.notfound.counsel_back.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int views;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int commentCount;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int likeCount;

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> postLikes = new ArrayList<>();

    @PrePersist
    public void createdAt() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.views = Math.max(0, this.views);
        this.commentCount = Math.max(0, this.commentCount);
        this.likeCount = Math.max(0, this.likeCount);
    }

    // 좋아요 수 증가
    public void increaseLikeCount() {
        this.likeCount++;
    }

    // 좋아요 수 감소
    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    // 조회수는 서비스/레포지토리에서 JPQL UPDATE로 원자적으로 증가
    public void incrementViews() {
        this.views++;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.setPost(this);
    }

    public void removeComment(Comment comment) {
        this.comments.remove(comment);
        comment.setPost(null);
    }

    public void addAttachment(Attachment attachment) {
        this.attachments.add(attachment);
        attachment.setPost(this);
    }

    public void removeAttachment(Attachment attachment) {
        this.attachments.remove(attachment);
        attachment.setPost(null);
    }

    public List<Attachment> clearAttachments() {
        List<Attachment> removedAttachments = new ArrayList<>(this.attachments);
        this.attachments.clear();
        return removedAttachments;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
