package np.org.nrb.pars.deal.model;

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
@Table(name = "p_dealtype")
public class DealType extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String value;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(name = "contact_mode", length = 20)
    private String contactMode;

    @Column(name = "is_instrument_code_applicable", nullable = false)
    private boolean instrumentCodeApplicable = true;

    @Column(name = "limit_type", length = 20)
    private String limitType;

    @Column(name = "limit_value")
    private BigDecimal limitValue;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
