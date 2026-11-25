package np.org.nrb.pars.ai.controller;

import np.org.nrb.pars.ai.domain.AiDtos;
import np.org.nrb.pars.ai.service.AiChatService;
import np.org.nrb.pars.ai.service.AiFeedbackService;
import np.org.nrb.pars.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@ConditionalOnProperty(prefix = "pars.ai", name = "enabled", havingValue = "true")
public class AiChatController {

    private final AiChatService chats;
    private final AiFeedbackService feedback;

    public AiChatController(AiChatService chats, AiFeedbackService feedback) {
        this.chats = chats;
        this.feedback = feedback;
    }

    @PostMapping("/chat")
    @PreAuthorize("hasAuthority('AI_CHAT')")
    public ApiResponse<AiDtos.AiChatResponse> chat(@Valid @RequestBody AiDtos.AiChatRequest request) {
        return ApiResponse.ok(chats.chat(request));
    }

    @GetMapping("/conversations")
    @PreAuthorize("hasAuthority('AI_CONVERSATION_READ')")
    public ApiResponse<List<AiDtos.AiConversationResponse>> conversations(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(chats.conversations(page, size));
    }

    @GetMapping("/conversations/{id}")
    @PreAuthorize("hasAuthority('AI_CONVERSATION_READ')")
    public ApiResponse<List<AiDtos.AiMessageResponse>> messages(@PathVariable UUID id) {
        return ApiResponse.ok(chats.messages(id));
    }

    @DeleteMapping("/conversations/{id}")
    @PreAuthorize("hasAuthority('AI_CONVERSATION_READ')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        chats.deleteConversation(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/feedback")
    @PreAuthorize("hasAuthority('AI_FEEDBACK')")
    public ApiResponse<Void> feedback(@Valid @RequestBody AiDtos.AiFeedbackRequest request) {
        feedback.submit(request);
        return ApiResponse.ok(null);
    }
}
