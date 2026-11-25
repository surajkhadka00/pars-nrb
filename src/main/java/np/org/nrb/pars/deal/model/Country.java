package np.org.nrb.pars.deal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import np.org.nrb.pars.common.audit.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "c_country")
public class Country extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String value;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
