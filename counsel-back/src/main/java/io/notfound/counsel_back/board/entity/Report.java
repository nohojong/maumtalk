package io.notfound.counsel_back.board.entity;

import io.notfound.counsel_back.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User reporter;   // 신고자

    private Long targetId;   // 대상 ID (게시글/댓글)

    @Enumerated(EnumType.STRING)
    private ReportTargetType targetType; // POST / COMMENT

    private String reason;

    private String justification; // AI의 판단 근거 저장 필드

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    private LocalDateTime createdAt;
    @PrePersist
    public void createdAt() {
        this.createdAt = LocalDateTime.now();
    }

    public void updateStatus(ReportStatus newStatus) {
        this.status = newStatus;
    }

    // 판단 근거를 업데이트하는 메서드 추가
    public void updateJustification(String justification) {
        this.justification = justification;
    }


}
