package io.notfound.counsel_back.conversation.service;

import io.notfound.counsel_back.conversation.entity.Conversation;
import io.notfound.counsel_back.conversation.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ContextManager {

    private final OpenAiChatModel openAiChatModel;
    private final ChatMemoryRepository chatMemoryRepository;
    private final ConversationRepository conversationRepository;
    private final OpenAiChatOptions options = OpenAiChatOptions.builder()
            .model(OpenAiApi.ChatModel.GPT_4_O)
            .build();

    private static final int MAX_CHAT_MEMORY_MESSAGES = 10;

    private static final String BASIC_SYSTEM_PROMPT = """
            당신은 사람들의 고민을 들어주는 AI 친구입니다. 당신의 목적은 사용자가 생각과 감정을 안전하고
            비판 없이 표현할 수 있는 공간을 제공하는 것입니다.
            당신은 치료사나 의료 전문가가 아니므로 절대 의학적 조언, 진단 또는 치료 계획을 제공해서는 안 됩니다.
            
            답변하기에 이용자의 문맥 파악에 정보가 부족하다고 판단되는 경우
            history : { } 중괄호 안쪽의 정보를 참고해주세요.
            
            만약 사용자가 자해 의도, 타인에 대한 해를 가하겠다는 표현,
            또는 즉각적인 위기 상황(생명·안전 위협)을 명시적으로 표현하면,
            다음 문구를 그대로 응답해야 합니다(다른 말은 추가하지 마세요):
            
            [지금 매우 어려운 시간을 보내고 계신 것 같습니다. 즉시 전문적인 도움을 받으시길 권합니다.
             대한민국에서는 24시간 이용 가능한 자살예방 상담전화 109 또는 긴급 상황 시 119(응급)
             또는 112(경찰)로 연락하실 수 있습니다.]
            
            당신은 오직 순수 텍스트로만 답변합니다. 마크다운 문법(예: #, *, -, ``` 등)은 사용하지 마세요.
            """;
    private static final String TITLE_GEN_PROMPT =
            "다음 대화에 적합한 대화 제목을 6단어 이내로 만들어 줘: \n";
    private static final String HISTORY_UPDATE_PROMPT = """
            다음 대화를 요약해서 정리해 줘. 요약은 순수 텍스트로만 하고,
            이모지나 마크다운 문법은 사용하지 말아줘.
            각 메시지를 다음 형식으로 요약해 줘:
            user: [사용자가 표현한 내용 요약]
            ai: [AI가 응답한 내용 요약]
            """;


    public ChatMemory getChatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(MAX_CHAT_MEMORY_MESSAGES)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
    }

    public Prompt getChatPrompt(String conversationId, ChatMemory chatMemory) {

        List<Message> recentMessages = chatMemory.get(conversationId);
        Conversation conversation = getConversationFromConversationId(conversationId);
        String currentHistory = conversation.getHistory();
        List<Message> finalMessages = new ArrayList<>();

        finalMessages.add(new SystemMessage(BASIC_SYSTEM_PROMPT));

        if (StringUtils.hasText(currentHistory)) {
            finalMessages.add(new SystemMessage(
                    "history : { " + currentHistory + "}"));
        }

        finalMessages.addAll(recentMessages);

        if (recentMessages.size() >= MAX_CHAT_MEMORY_MESSAGES) {
            updateHistoryAsync(conversationId, recentMessages);
            chatMemory.clear(conversationId);
        }

        return new Prompt(finalMessages, options);
    }

    public Prompt getTitleGenerationPrompt(String firstChat) {
        String requestMessage = TITLE_GEN_PROMPT + firstChat;
        return new Prompt(new SystemMessage(requestMessage), options);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> updateHistoryAsync(String conversationId, List<Message> recentMessages) {
        performHistoryUpdate(conversationId, recentMessages);
        return CompletableFuture.completedFuture(null);
    }

    private void performHistoryUpdate(String conversationId, List<Message> recentMessages) {
        // history 업데이트만 필요하므로 chatMessages 없이 조회
        Conversation conversation = conversationRepository.findByIdWithoutChatMessages(
                        Long.parseLong(conversationId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "대화를 찾을 수 없습니다: " + conversationId));

        String historyBefore = conversation.getHistory() != null ? conversation.getHistory() : "";

        StringBuilder conversationText = new StringBuilder();
        for (Message message : recentMessages) {
            if (message instanceof UserMessage) {
                conversationText.append("user: ").append(message.getText()).append("\n");
            } else if (message instanceof AssistantMessage) {
                conversationText.append("ai: ").append(message.getText()).append("\n");
            }
        }

        String requestContent = HISTORY_UPDATE_PROMPT + "\n\n대화 내용:\n" + conversationText.toString();
        List<Message> historyUpdateRequest = List.of(new SystemMessage(requestContent));

        Prompt historyPrompt = new Prompt(historyUpdateRequest, options);
        String historyRecent = openAiChatModel.call(historyPrompt).getResult().getOutput().getText();

        String updatedHistory = historyBefore + "\n" + historyRecent;
        conversation.updateHistory(updatedHistory);

        // 명시적으로 저장 (merge 대신 save 사용)
        conversationRepository.save(conversation);
    }

//    @Async
//    @Transactional
//    public void updateHistory(String conversationId, List<Message> recentMessages) {
//        performHistoryUpdate(conversationId, recentMessages);
//    }

//    private void performHistoryUpdate(String conversationId, List<Message> recentMessages) {
//
//        Conversation conversation = getConversationFromConversationId(conversationId);
//        String historyBefore = conversation.getHistory();
//
//        // 메시지를 구분 가능한 형태로 변환
//        StringBuilder conversationText = new StringBuilder();
//        for (Message message : recentMessages) {
//            if (message instanceof UserMessage) {
//                conversationText.append("user: ").append(message.getText()).append("\n");
//            } else if (message instanceof AssistantMessage) {
//                conversationText.append("ai: ").append(message.getText()).append("\n");
//            }
//        }
//
//        // 히스토리 업데이트 요청 구성
//        String requestContent = HISTORY_UPDATE_PROMPT + "\n\n대화 내용:\n" + conversationText.toString();
//        List<Message> historyUpdateRequest = List.of(new SystemMessage(requestContent));
//
//        Prompt historyPrompt = new Prompt(historyUpdateRequest, options);
//        String historyRecent = openAiChatModel.call(historyPrompt).getResult().getOutput().getText();
//
//        String updatedHistory = historyBefore + "\n" + historyRecent;
//
//        conversation.updateHistory(updatedHistory);
//        conversationRepository.save(conversation);
//    }

    private Conversation getConversationFromConversationId(String conversationIdStr) {
        Long conversationId = Long.parseLong(conversationIdStr);

        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "대화를 찾을 수 없습니다: " + conversationId));
    }
}
