package io.notfound.counsel_back.conversation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = @Index(name = "idx_conversation_created_at", columnList = "conversation_id, createdAt"))
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sender sender;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ChatMessage(Sender sender, String message, Conversation conversation) {
        this.sender = sender;
        this.message = message;
        this.conversation = conversation;
    }
}
