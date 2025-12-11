package io.notfound.counsel_back.board.service;

import io.notfound.counsel_back.board.dto.AIModerationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIModerationService {

    private final OpenAiChatModel openAiChatModel;

    public AIModerationResponse moderateComment(String postContent, String commentContent, String reportReason) {
        ChatClient chatClient = ChatClient.create(openAiChatModel);

        var outputConverter = new BeanOutputConverter<>(AIModerationResponse.class);

        String promptString = """
            당신은 익명 고민 상담 애플리케이션의 콘텐츠 중재 전문가입니다.
            사용자들이 올린 민감한 고민 게시글과 그에 대한 댓글, 그리고 신고 사유를 분석하여 댓글이 서비스 정책을 위반했는지 판단해야 합니다.

            [판단 정책]
            - 신고 승인 (APPROVE_REPORT): 댓글이 욕설, 비난, 인신공격, 조롱, 스팸, 주제와 무관한 내용, 무성의한 답변 등 게시글 작성자에게 상처를 주거나 도움이 되지 않는 경우.
            - 신고 반려 (REJECT_REPORT): 댓글이 건설적이고, 공감하며, 위로가 되거나, 현실적인 조언을 담고 있어 문제가 없는 경우. 신고 사유가 타당하지 않은 경우.

            [분석 대상]
            1. 원본 게시글: {postContent}
            2. 신고된 댓글: {commentContent}
            3. 신고 사유: {reportReason}

            위 정보를 바탕으로 신고를 '승인'할지 '반려'할지 결정하고, 그에 대한 핵심적인 판단 근거를 한 문장으로 요약해 주세요.
            {format}
            """;

        PromptTemplate promptTemplate = new PromptTemplate(promptString);
        Prompt prompt = promptTemplate.create(Map.of(
                "postContent", postContent,
                "commentContent", commentContent,
                "reportReason", reportReason,
                "format", outputConverter.getFormat()
        ));

        // 2. ChatClient Fluent API를 사용하여 더 간결하게 호출 및 변환
        return chatClient.prompt(prompt)
                .call()
                .entity(outputConverter);
    }
}