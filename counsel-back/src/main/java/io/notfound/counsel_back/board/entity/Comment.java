package io.notfound.counsel_back.board.entity;

import io.notfound.counsel_back.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private User writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(nullable = false)
    @Builder.Default
    @ColumnDefault("false") // 기본값은 false
    private boolean isBlinded = false;

    private LocalDateTime createdAt;

    // Setter를 외부에서 직접 호출하지 못하도록 package-private으로 변경
    // 연관관계 편의 메서드를 통해서만 post가 설정되도록 유도
    void setPost(Post post) {
        this.post = post;
    }

    @PrePersist
    public void createdAt() {
        this.createdAt = LocalDateTime.now();
    }

    // 내용 수정을 위한 메서드
    public void update(String content) {
        this.content = content;
    }

    // 블라인드 상태를 변경하는 메서드 추가
    public void blind() {
        this.isBlinded = true;
        this.content = "관리자에 의해 블라인드 처리된 댓글입니다.";
    }
}