package np.org.nrb.pars.ai.service;

import np.org.nrb.pars.monitoring.service.AuditLogService;
import np.org.nrb.pars.system.model.SysConfig;
import np.org.nrb.pars.system.repository.SysConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Runtime AI kill switch. Core PARS remains fully functional when AI generation
 * or external ingestion is disabled by an authorized administrator.
 */
@Service
public class AiKillSwitchService {

    public static final String KEY = "ai.kill.switch";

    private final SysConfigRepository configs;
    private final AuditLogService audit;

    public AiKillSwitchService(SysConfigRepository configs, AuditLogService audit) {
        this.configs = configs;
        this.audit = audit;
    }

    public boolean isDisabled() {
        return configs.findById(KEY)
                .map(config -> "ON".equalsIgnoreCase(String.valueOf(config.getValue()).trim()))
                .orElse(false);
    }

    @Transactional
    public void setDisabled(boolean disabled, UUID userId, String reason) {
        Instant now = Instant.now();
        SysConfig config = configs.findById(KEY).orElseGet(SysConfig::new);
        if (config.getName() == null) {
            config.setName(KEY);
            config.setCreatedAt(now);
        }
        config.setValue(disabled ? "ON" : "OFF");
        config.setUpdatedBy(userId);
        config.setUpdatedAt(now);
        configs.save(config);
        audit.record("POST", "/api/v1/admin/ai/kill-switch", 200,
                "AI kill switch " + (disabled ? "enabled" : "disabled") + "; reason=" + reason, 0, userId);
    }

    public Map<String, Object> status() {
        SysConfig config = configs.findById(KEY).orElse(null);
        return Map.of(
                "disabled", isDisabled(),
                "updatedBy", config == null || config.getUpdatedBy() == null ? "" : config.getUpdatedBy().toString(),
                "updatedAt", config == null || config.getUpdatedAt() == null ? "" : config.getUpdatedAt().toString());
    }
}
