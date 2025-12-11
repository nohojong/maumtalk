package io.notfound.counsel_back.conversation.repository;

import io.notfound.counsel_back.conversation.entity.Conversation;
import io.notfound.counsel_back.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    // 사용자로 대화 목록 조회 (가장 최신순으로)
    List<Conversation> findByUserOrderByCreatedAtDesc(User user);

    @EntityGraph("Conversation.withChatMessages")
    Optional<Conversation> findWithChatMessagesById(Long id);

    @Query("SELECT c FROM Conversation c JOIN FETCH c.chatMessages WHERE c.id = :id")
    Optional<Conversation> findByIdWithChatMessages(@Param("id") Long id);

    @Query("SELECT c FROM Conversation c WHERE c.id = :id")
    Optional<Conversation> findByIdWithoutChatMessages(@Param("id") Long id);

    // 특정 사용자의 특정 대화 조회 (권한 확인용)
    Optional<Conversation> findByIdAndUser(Long id, User user);
}
