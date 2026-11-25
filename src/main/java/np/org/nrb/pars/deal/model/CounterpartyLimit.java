package np.org.nrb.pars.deal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import np.org.nrb.pars.common.audit.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "p_counterpartylimit")
public class CounterpartyLimit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "p_counterparty_id", nullable = false)
    private Counterparty counterparty;

    @Column(name = "date_from")
    private Instant dateFrom;

    @Column(name = "date_to")
    private Instant dateTo;

    @Column(length = 255)
    private String description;

    @Column(name = "limit_criteria", length = 40)
    private String limitCriteria;

    @Column(name = "limit_type", length = 20)
    private String limitType;

    @Column(name = "limit_value")
    private BigDecimal limitValue;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
