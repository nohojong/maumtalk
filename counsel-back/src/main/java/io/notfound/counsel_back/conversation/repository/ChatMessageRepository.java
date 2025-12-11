package io.notfound.counsel_back.conversation.repository;

import io.notfound.counsel_back.conversation.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}
