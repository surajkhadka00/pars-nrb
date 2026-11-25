package np.org.nrb.pars.deal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import np.org.nrb.pars.common.audit.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "c_currency")
public class Currency extends BaseEntity {

    @Column(name = "iso_code", nullable = false, unique = true, length = 3)
    private String isoCode;

    @Column(name = "cur_symbol", length = 10)
    private String curSymbol;

    @Column(nullable = false)
    private String description;

    @Column(name = "std_precision", nullable = false)
    private int stdPrecision = 2;

    @Column(name = "costing_precision", nullable = false)
    private int costingPrecision = 4;

    @Column(name = "is_euro", nullable = false)
    private boolean euro;

    @Column(name = "is_emu_member", nullable = false)
    private boolean emuMember;

    @Column(name = "emu_entry_date")
    private Instant emuEntryDate;

    @Column(name = "emu_rate")
    private BigDecimal emuRate;

    @Column(name = "round_off_factor")
    private BigDecimal roundOffFactor;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
