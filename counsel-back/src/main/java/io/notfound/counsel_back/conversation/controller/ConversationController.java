package io.notfound.counsel_back.conversation.controller;

import io.notfound.counsel_back.conversation.dto.ChatRequest;
import io.notfound.counsel_back.conversation.dto.ConversationDetailResponse;
import io.notfound.counsel_back.conversation.dto.ConversationListResponse;
import io.notfound.counsel_back.conversation.dto.ConversationUpdateRequest;
import io.notfound.counsel_back.conversation.service.ChatService;
import io.notfound.counsel_back.conversation.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ChatService chatService;
    private final ConversationService conversationService; // 새 서비스 주입

    // 스트리밍 채팅 엔드포인트 (기존 chat 기능을 /api/conversations/chat으로 변경)
    @PostMapping(value = "/chat")
    public Flux<String> chat(
            @RequestBody ChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        return chatService.completeChat(request, email);
    }

    // 모든 대화 목록 조회
    @GetMapping
    public ResponseEntity<List<ConversationListResponse>> getConversations(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        List<ConversationListResponse> conversations = conversationService.getConversationsByUser(email);

        return ResponseEntity.ok(conversations);
    }

    // 특정 대화 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ConversationDetailResponse> getConversationDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        ConversationDetailResponse conversation = conversationService.getConversationDetail(id, email);
        return ResponseEntity.ok(conversation);
    }

    // 대화 제목 및 요약 수정
    @PutMapping("/{id}")
    public ResponseEntity<ConversationDetailResponse> updateConversation(
            @PathVariable Long id,
            @RequestBody ConversationUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        ConversationDetailResponse updatedConversation = conversationService.updateConversation(id, request, email);
        return ResponseEntity.ok(updatedConversation);
    }

    // 대화 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        conversationService.deleteConversation(id, email);
        return ResponseEntity.noContent().build();
    }
}