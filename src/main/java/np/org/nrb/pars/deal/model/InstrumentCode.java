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

@Getter
@Setter
@Entity
@Table(name = "p_instrumentcode")
public class InstrumentCode extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "p_dealtype_id", nullable = false)
    private DealType dealType;

    @Column(nullable = false, unique = true, length = 20)
    private String value;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
