package io.notfound.counsel_back.conversation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDetailResponse {
    private Long id;
    private String title;
    private String memo;
    private LocalDateTime createdAt;
    private List<ChatMessageResponse> messages;
}