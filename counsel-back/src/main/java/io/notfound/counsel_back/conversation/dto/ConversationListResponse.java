package io.notfound.counsel_back.conversation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConversationListResponse {
    private Long id;
    private String title;
    private LocalDateTime createdAt;
}
