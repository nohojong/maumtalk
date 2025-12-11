package io.notfound.counsel_back.conversation.service;

import io.notfound.counsel_back.conversation.dto.ChatMessageResponse;
import io.notfound.counsel_back.conversation.dto.ConversationDetailResponse;
import io.notfound.counsel_back.conversation.dto.ConversationListResponse;
import io.notfound.counsel_back.conversation.dto.ConversationUpdateRequest;
import io.notfound.counsel_back.conversation.entity.Conversation;
import io.notfound.counsel_back.conversation.repository.ConversationRepository;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    
    // 전체 대화 목록 조회
    @Transactional(readOnly = true)
    public List<ConversationListResponse> getConversationsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + email));
        return conversationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::convertToListDto)
                .toList();
    }

    // 특정 대화 상세 조회
    @Transactional(readOnly = true)
    public ConversationDetailResponse getConversationDetail(Long conversationId, String email) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "대화를 찾을 수 없습니다: " + conversationId));

        if (!conversation.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 대화에 접근할 권한이 없습니다.");
        }
        return convertToDetailDto(conversation);
    }

    // 대화 수정 (제목, 요약)
    @Transactional
    public ConversationDetailResponse updateConversation(
            Long conversationId, ConversationUpdateRequest request, String email) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "대화를 찾을 수 없습니다: " + conversationId)
                );
        
        if (!conversation.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 대화를 수정할 권한이 없습니다.");
        }

        boolean updated = false;

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            conversation.updateTitle(request.getTitle());
            updated = true;
        }
        if (request.getMemo() != null) {
            conversation.updateMemo(request.getMemo());
            updated = true;
        }

        if (updated) {
            conversationRepository.save(conversation);
        }

        return convertToDetailDto(conversation);
    }

    // 첫번째 ai 응답을 통한 제목 생성, 위쪽 updatedConversation 을 통해서 추후 변경 가능
    @Transactional
    public void updateConversationTitle(Long conversationId, String newTitle) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "대화를 찾을 수 없습니다." + conversationId)
                );
        conversation.updateTitle(newTitle);
        conversationRepository.save(conversation);
    }

    // 대화 삭제
    @Transactional
    public void deleteConversation(Long conversationId, String email) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "대화를 찾을 수 없습니다: " + conversationId
                        )
                );

        if (!conversation.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 대화를 삭제할 권한이 없습니다.");
        }
        conversationRepository.delete(conversation);
    }

    // DTO 변환 메서드 (내부적으로 사용)
    private ConversationListResponse convertToListDto(Conversation conversation) {
        return new ConversationListResponse(
                conversation.getId(),
                conversation.getTitle() != null ? conversation.getTitle() : "새로운 고민 상담",
                conversation.getCreatedAt()
        );
    }

    private ConversationDetailResponse convertToDetailDto(Conversation conversation) {
        List<ChatMessageResponse> chatMessages = conversation.getChatMessages().stream()
                .map(msg -> new ChatMessageResponse(
                        msg.getId(), msg.getMessage(), msg.getSender().name())
                )
                .collect(Collectors.toList());
        return new ConversationDetailResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getMemo(),
                conversation.getCreatedAt(),
                chatMessages
        );
    }
}