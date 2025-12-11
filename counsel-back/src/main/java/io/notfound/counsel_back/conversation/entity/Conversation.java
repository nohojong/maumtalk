package io.notfound.counsel_back.conversation.entity;

import io.notfound.counsel_back.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String history;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public void updateTitle(String title) { this.title = title; }
    public void updateMemo(String memo) { this.memo = memo; }
    public void updateHistory(String history) { this.history = history; }

    public void addChatMessage(ChatMessage chatMessage) {
        this.chatMessages.add(chatMessage);
        chatMessage.setConversation(this);
    }

    @Builder
    public Conversation(User user) {
        this.user = user;
    }
}