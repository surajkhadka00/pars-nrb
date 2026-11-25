package np.org.nrb.pars.usermanagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import np.org.nrb.pars.common.audit.BaseEntity;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "ad_position")
public class Position extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String value;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "limit_type", length = 20)
    private String limitType;

    @Column(name = "limit_value")
    private BigDecimal limitValue;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
