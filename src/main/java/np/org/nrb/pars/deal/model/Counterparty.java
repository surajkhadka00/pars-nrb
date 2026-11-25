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
import np.org.nrb.pars.usermanagement.model.AppUser;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "p_counterparty")
public class Counterparty extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String value;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_user_id")
    private AppUser user;

    @Column(name = "bloomberg_code", length = 40)
    private String bloombergCode;

    @Column(name = "c_country_id")
    private UUID countryId;

    @Column(length = 2000)
    private String comments;

    @Column(name = "contact_name", length = 120)
    private String contactName;

    @Column(length = 100)
    private String email;

    @Column(name = "investment_grade", length = 40)
    private String investmentGrade;

    @Column(name = "is_own_bank", nullable = false)
    private boolean ownBank;

    @Column(name = "limit_type", length = 20)
    private String limitType;

    @Column(name = "limit_value")
    private BigDecimal limitValue;

    @Column(length = 120)
    private String location;

    @Column(length = 30)
    private String phone;

    @Column(length = 100)
    private String position;

    @Column(name = "reuters_dealing_code", length = 40)
    private String reutersDealingCode;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
