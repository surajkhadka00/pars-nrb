package np.org.nrb.pars.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import np.org.nrb.pars.ai.repository.AiConversationRepository;
import np.org.nrb.pars.ai.config.ParsAiProperties;
import np.org.nrb.pars.ai.domain.AiDtos;
import np.org.nrb.pars.ai.rag.RagAnswerService;
import np.org.nrb.pars.common.exception.Exceptions;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "pars.ai", name = "enabled", havingValue = "true")
public class AiChatService {

    private final AiConversationRepository conversations;
    private final RagAnswerService answerService;
    private final AiPermissionService permissions;
    private final ObjectMapper mapper;
    private final ParsAiProperties properties;

    public AiChatService(AiConversationRepository conversations, RagAnswerService answerService,
                         AiPermissionService permissions, ObjectMapper mapper, ParsAiProperties properties) {
        this.conversations = conversations;
        this.answerService = answerService;
        this.permissions = permissions;
        this.mapper = mapper;
        this.properties = properties;
    }

    public AiDtos.AiChatResponse chat(AiDtos.AiChatRequest request) {
        UUID owner = permissions.userId();
        UUID conversationId = request.conversationId();
        if (conversationId == null) {
            conversationId = conversations.create(owner, titleFrom(request.question()), properties.getConversationRetention());
        } else if (conversations.findOwnedId(conversationId, owner).isEmpty()) {
            throw Exceptions.forbidden("Conversation not found or not owned by this user");
        }

        RagAnswerService.Answer answer = answerService.answer(request.question());
        String traceId = MDC.get("traceId");
        UUID userMessageId = UUID.randomUUID();
        UUID assistantMessageId = UUID.randomUUID();
        conversations.insertMessage(userMessageId, conversationId, "USER", request.question(), "[]", false, traceId);
        try {
            conversations.insertMessage(assistantMessageId, conversationId, "ASSISTANT", answer.text(),
                    mapper.writeValueAsString(answer.sources()), answer.insufficientEvidence(), traceId);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to persist AI answer", e);
        }
        conversations.touch(conversationId);
        return new AiDtos.AiChatResponse(answer.text(), conversationId, assistantMessageId,
                answer.sources(), answer.insufficientEvidence(), traceId, properties.getChatModel());
    }

    private String titleFrom(String question) {
        String clean = question.trim().replaceAll("\\s+", " ");
        return clean.length() > 80 ? clean.substring(0, 80) : clean;
    }

    public List<AiDtos.AiConversationResponse> conversations(int page, int size) {
        return conversations.listForOwner(permissions.userId(), page, size);
    }

    public List<AiDtos.AiMessageResponse> messages(UUID conversationId) {
        if (conversations.findOwnedId(conversationId, permissions.userId()).isEmpty()) {
            throw Exceptions.forbidden("Conversation not found or not owned by this user");
        }
        return conversations.messages(conversationId);
    }

    public void deleteConversation(UUID id) {
        if (conversations.findOwnedId(id, permissions.userId()).isEmpty()) {
            throw Exceptions.forbidden("Conversation not found or not owned by this user");
        }
        conversations.delete(id);
    }
}
