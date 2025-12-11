package io.notfound.counsel_back.user.entity;

import io.notfound.counsel_back.board.entity.PostLike;
import io.notfound.counsel_back.conversation.entity.Conversation;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String userName;

    @Column(nullable = true)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    // OAuth2 제공자 ID (Google의 sub, Naver의 id 등)
    @Column(unique = true)
    private String providerId;

    // OAuth2 제공자 (GOOGLE, NAVER 등)
    @Enumerated(EnumType.STRING)
    private ProviderType provider;

    // 일반 사용자가 좋아요한 게시글 리스트
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> postLikes = new ArrayList<>();

    // 사용자가 참여한 대화 리스트
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Conversation> conversations = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(length = 500)
    private String refreshToken;

    @Column(nullable = true)
    private LocalDateTime accessUntil;  // 접근 만료 시간 필드

    @Builder
    public User(String email, String userName, String password, UserRole role,
                String providerId, ProviderType provider) {
        this.email = email;
        this.userName = userName;
        this.password = password;
        this.role = role != null ? role : UserRole.USER;
        this.providerId = providerId;
        this.provider = provider;
    }

    /**
     * Refreshes the refresh token.
     * 리프레시 토큰을 업데이트합니다.
     */
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * OAuth2 사용자인지 확인합니다.
     */
    public boolean isOAuth2User() {
        return providerId != null && provider != null;
    }

    /**
     * 일반 회원가입 사용자인지 확인합니다.
     */
    public boolean isLocalUser() {
        return password != null && !isOAuth2User();
    }

    /**
     * 제공자를 업데이트합니다.
     */
    public void updateProvider(ProviderType provider, String providerId) {
        this.provider = provider;
        this.providerId = providerId;
    }

    // AccessUntil 필드에 대한 getter와 setter 추가
    public LocalDateTime getAccessUntil() {
        return this.accessUntil;
    }

    public void setAccessUntil(LocalDateTime accessUntil) {
        this.accessUntil = accessUntil;
    }
}
