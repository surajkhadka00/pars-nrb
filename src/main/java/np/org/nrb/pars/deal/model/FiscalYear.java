package np.org.nrb.pars.deal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import np.org.nrb.pars.common.audit.BaseEntity;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "p_fiscalyear")
public class FiscalYear extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String value;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
}
