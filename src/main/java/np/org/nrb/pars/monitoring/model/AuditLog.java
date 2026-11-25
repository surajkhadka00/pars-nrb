package np.org.nrb.pars.monitoring.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import np.org.nrb.pars.common.audit.BaseEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "ad_audit_log")
public class AuditLog extends BaseEntity {

    @Column(nullable = false, length = 16)
    private String method;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private int status;

    @Column(columnDefinition = "text")
    private String payload;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "user_id")
    private UUID userId;
}
