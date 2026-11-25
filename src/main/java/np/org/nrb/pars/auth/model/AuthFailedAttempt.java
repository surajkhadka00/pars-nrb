package np.org.nrb.pars.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import np.org.nrb.pars.common.audit.BaseEntity;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "ad_auth_failed_attempt")
public class AuthFailedAttempt extends BaseEntity {

    @Column(nullable = false, length = 64)
    private String username;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt;

    @Column(nullable = false, length = 64)
    private String reason;
}
