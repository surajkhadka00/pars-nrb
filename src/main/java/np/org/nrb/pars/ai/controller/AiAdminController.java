package np.org.nrb.pars.ai.controller;

import np.org.nrb.pars.ai.config.ParsAiProperties;
import np.org.nrb.pars.ai.domain.AiDtos;
import np.org.nrb.pars.ai.repository.AiJobRepository;
import np.org.nrb.pars.ai.service.AiDocumentService;
import np.org.nrb.pars.ai.service.AiKillSwitchService;
import np.org.nrb.pars.ai.service.AiPermissionService;
import np.org.nrb.pars.common.api.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai/admin")
@ConditionalOnProperty(prefix = "pars.ai", name = "enabled", havingValue = "true")
public class AiAdminController {

    private final ParsAiProperties properties;
    private final AiDocumentService documents;
    private final AiJobRepository jobs;
    private final JdbcTemplate jdbc;
    private final AiKillSwitchService killSwitch;
    private final AiPermissionService permissions;

    public AiAdminController(ParsAiProperties properties, AiDocumentService documents, AiJobRepository jobs,
                             JdbcTemplate jdbc, AiKillSwitchService killSwitch, AiPermissionService permissions) {
        this.properties = properties;
        this.documents = documents;
        this.jobs = jobs;
        this.jdbc = jdbc;
        this.killSwitch = killSwitch;
        this.permissions = permissions;
    }

    @GetMapping("/kill-switch")
    @PreAuthorize("hasAuthority('AI_ADMIN')")
    public ApiResponse<Map<String, Object>> killSwitchStatus() {
        return ApiResponse.ok(killSwitch.status());
    }

    @PostMapping("/kill-switch")
    @PreAuthorize("hasAuthority('AI_ADMIN')")
    public ApiResponse<Map<String, Object>> setKillSwitch(
            @RequestParam boolean disabled, @RequestParam(defaultValue = "") String reason) {
        killSwitch.setDisabled(disabled, permissions.userId(), reason);
        return ApiResponse.ok(killSwitch.status());
    }

    @GetMapping("/status")
    @PreAuthorize("hasAuthority('AI_ADMIN')")
    public ApiResponse<AiDtos.AiMetricsResponse> status() {
        // Status details are exposed through /actuator/health/ai; never expose keys here.
        return metrics();
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasAuthority('AI_METRICS_READ')")
    public ApiResponse<AiDtos.AiMetricsResponse> metrics() {
        Long questions = jdbc.queryForObject("SELECT COUNT(*) FROM ai.ai_usage_events WHERE event_type = 'CHAT'", Long.class);
        Long failed = jdbc.queryForObject("SELECT COUNT(*) FROM ai.ai_usage_events WHERE event_type = 'CHAT_FAILED'", Long.class);
        Long published = jdbc.queryForObject("SELECT COUNT(*) FROM ai.ai_documents WHERE publication_status = 'PUBLISHED' AND active_version", Long.class);
        Long failedJobs = jdbc.queryForObject("SELECT COUNT(*) FROM ai.ai_ingestion_jobs WHERE status = 'FAILED'", Long.class);
        Long feedbackCount = jdbc.queryForObject("SELECT COUNT(*) FROM ai.ai_feedback", Long.class);
        Double average = jdbc.queryForObject("SELECT COALESCE(AVG(rating), 0) FROM ai.ai_feedback", Double.class);
        return ApiResponse.ok(new AiDtos.AiMetricsResponse(
                questions == null ? 0 : questions, failed == null ? 0 : failed,
                published == null ? 0 : published, failedJobs == null ? 0 : failedJobs,
                feedbackCount == null ? 0 : feedbackCount, average == null ? 0 : average));
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasAuthority('AI_ADMIN')")
    public ApiResponse<List<AiDtos.AiJobResponse>> jobs() {
        return ApiResponse.ok(jobs.list(0, 100));
    }

    @GetMapping("/config")
    @PreAuthorize("hasAuthority('AI_ADMIN')")
    public ApiResponse<java.util.Map<String, Object>> config() {
        return ApiResponse.ok(java.util.Map.of(
                "enabled", properties.isEnabled(),
                "provider", properties.getProvider(),
                "chatModel", properties.getChatModel(),
                "embeddingModel", properties.getEmbeddingModel(),
                "embeddingDimensions", properties.getEmbeddingDimensions(),
                "chunkSizeTokens", properties.getChunkSizeTokens(),
                "chunkOverlapTokens", properties.getChunkOverlapTokens(),
                "retrievalTopK", properties.getRetrievalTopK(),
                "evidenceThreshold", properties.getEvidenceThreshold()));
    }
}
