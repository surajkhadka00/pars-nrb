package np.org.nrb.pars.ai.service;

import np.org.nrb.pars.ai.repository.AiConversationRepository;
import np.org.nrb.pars.ai.domain.AiDtos;
import np.org.nrb.pars.common.exception.Exceptions;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AiFeedbackService {

    private final AiConversationRepository conversations;
    private final AiPermissionService permissions;

    public AiFeedbackService(AiConversationRepository conversations, AiPermissionService permissions) {
        this.conversations = conversations;
        this.permissions = permissions;
    }

    public void submit(AiDtos.AiFeedbackRequest request) {
        if (conversations.findOwnedMessage(request.messageId(), permissions.userId()).isEmpty()) {
            throw Exceptions.forbidden("Message not found or not owned by this user");
        }
        conversations.saveFeedback(UUID.randomUUID(), request.messageId(), permissions.userId(),
                request.rating(), request.comment());
    }
}
