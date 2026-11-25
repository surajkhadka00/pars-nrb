package np.org.nrb.pars.ai.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Request/response records for the AI REST API. Kept together to avoid DTO sprawl. */
public final class AiDtos {
    private AiDtos() {}

    public record AiChatRequest(
            @NotBlank @Size(max = 8000) String question,
            UUID conversationId
    ) {}

    public record AiSourceResponse(
            UUID documentId,
            String documentTitle,
            Integer docVersion,
            Integer page,
            String section,
            UUID chunkId,
            BigDecimal score,
            String sourceStatus,
            String sourceUrl
    ) {}

    public record AiChatResponse(
            String answer,
            UUID conversationId,
            UUID messageId,
            List<AiSourceResponse> sources,
            boolean insufficientEvidence,
            String traceId,
            String model
    ) {}

    public record AiConversationResponse(
            UUID id,
            String title,
            Instant createdAt,
            Instant updatedAt,
            int messageCount
    ) {}

    public record AiMessageResponse(
            UUID id,
            String role,
            String content,
            List<AiSourceResponse> sources,
            boolean insufficientEvidence,
            Instant createdAt
    ) {}

    public record AiFeedbackRequest(
            @jakarta.validation.constraints.NotNull UUID messageId,
            @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(5) int rating,
            @Size(max = 1000) String comment
    ) {}

    public record AiDocumentRequest(
            @NotBlank @Size(max = 300) String title,
            @Size(max = 2000) String description,
            @Size(max = 120) String category,
            @NotBlank String accessClassification,
            Integer docVersion
    ) {}

    public record AiDocumentResponse(
            UUID id,
            String title,
            String originalFilename,
            String mimeType,
            String checksum,
            long fileSize,
            String category,
            String accessClassification,
            String publicationStatus,
            int docVersion,
            boolean activeVersion,
            Instant uploadedAt,
            Instant publishedAt,
            String embeddingModel,
            Instant lastIndexedAt
    ) {}

    public record AiJobResponse(
            UUID id,
            UUID documentId,
            String status,
            String stage,
            int progress,
            int attempts,
            String errorMessage,
            Instant createdAt,
            Instant finishedAt
    ) {}

    public record AiMetricsResponse(
            long totalQuestions,
            long failedQuestions,
            long publishedDocuments,
            long failedJobs,
            long feedbackCount,
            double averageFeedback
    ) {}
}
