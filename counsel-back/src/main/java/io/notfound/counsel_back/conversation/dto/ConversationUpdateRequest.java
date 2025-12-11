package io.notfound.counsel_back.conversation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConversationUpdateRequest {
    private String title;
    private String memo;
}