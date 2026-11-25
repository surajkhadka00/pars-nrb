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
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "p_deal")
public class Deal extends BaseEntity {

    @Column(name = "document_no", nullable = false, unique = true, length = 40)
    private String documentNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "p_dealtype_id", nullable = false)
    private DealType dealType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "p_instrumentcode_id")
    private InstrumentCode instrumentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private AppUser approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by_id")
    private AppUser rejectedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_id")
    private AppUser submittedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_id")
    private AppUser verifiedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "c_currency_id")
    private Currency currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "c_counter_currency_id")
    private Currency counterCurrency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "p_our_nostro_id")
    private Nostro ourNostro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "p_their_nostro_id")
    private Nostro theirNostro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "p_counterparty_id")
    private Counterparty counterparty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ii_counterparty_id")
    private Counterparty iiCounterparty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iii_counterparty_id")
    private Counterparty iiiCounterparty;

    @Column(name = "alloted_amt")
    private BigDecimal allotedAmt;

    @Column(name = "alloted_units")
    private BigDecimal allotedUnits;

    private BigDecimal amount;

    @Column(name = "bid_amt")
    private BigDecimal bidAmt;

    @Column(name = "contra_amt")
    private BigDecimal contraAmt;

    private BigDecimal coupon;

    @Column(name = "discounted_price_per_unit")
    private BigDecimal discountedPricePerUnit;

    @Column(name = "discounted_value")
    private BigDecimal discountedValue;

    @Column(name = "effective_yield")
    private BigDecimal effectiveYield;

    @Column(name = "exchange_rate")
    private BigDecimal exchangeRate;

    @Column(name = "ii_rate")
    private BigDecimal iiRate;

    @Column(name = "iii_rate")
    private BigDecimal iiiRate;

    @Column(name = "interest_rate")
    private BigDecimal interestRate;

    @Column(name = "payable_amt")
    private BigDecimal payableAmt;

    @Column(name = "rollover_previous_rate")
    private BigDecimal rolloverPreviousRate;

    @Column(name = "rollover_value_amount")
    private BigDecimal rolloverValueAmount;

    @Column(name = "total_cost")
    private BigDecimal totalCost;

    @Column(name = "total_payable")
    private BigDecimal totalPayable;

    @Column(name = "tenure_in_days")
    private Integer tenureInDays;

    @Column(name = "bbg_code", length = 40)
    private String bbgCode;

    @Column(name = "bond_code", length = 40)
    private String bondCode;

    @Column(name = "contact_mode", length = 20)
    private String contactMode;

    @Column(name = "cusip_no", length = 20)
    private String cusipNo;

    @Column(length = 2000)
    private String description;

    @Column(length = 4)
    private String direction;

    @Column(length = 4)
    private String direction2;

    @Column(name = "doc_action", nullable = false, length = 2)
    private String docAction = "CO";

    @Column(name = "doc_status", nullable = false, length = 2)
    private String docStatus = "DR";

    @Column(length = 2000)
    private String help;

    @Column(name = "ii_contact_mode", length = 20)
    private String iiContactMode;

    @Column(name = "ii_contact_person_or_code", length = 120)
    private String iiContactPersonOrCode;

    @Column(name = "iii_contact_mode", length = 20)
    private String iiiContactMode;

    @Column(name = "iii_contact_person_or_code", length = 120)
    private String iiiContactPersonOrCode;

    @Column(name = "interest_payment_frequency", length = 40)
    private String interestPaymentFrequency;

    @Column(name = "market_type", length = 40)
    private String marketType;

    @Column(name = "memo_ref_no", length = 40)
    private String memoRefNo;

    @Column(name = "our_custodian", length = 120)
    private String ourCustodian;

    @Column(name = "their_custodian", length = 120)
    private String theirCustodian;

    @Column(name = "reject_reason", length = 2000)
    private String rejectReason;

    @Column(name = "rollover_deal_ref_no", length = 40)
    private String rolloverDealRefNo;

    @Column(name = "rollover_option", length = 4)
    private String rolloverOption;

    @Column(name = "rollover_remarks", length = 2000)
    private String rolloverRemarks;

    @Column(length = 40)
    private String series;

    @Column(length = 120)
    private String trader;

    @Column(name = "bidding_date")
    private Instant biddingDate;

    @Column(name = "date_approved")
    private Instant dateApproved;

    @Column(name = "date_rejected")
    private Instant dateRejected;

    @Column(name = "date_submitted")
    private Instant dateSubmitted;

    @Column(name = "date_verified")
    private Instant dateVerified;

    @Column(name = "deal_date")
    private Instant dealDate;

    @Column(name = "deal_time")
    private Instant dealTime;

    @Column(name = "last_interest_payment_date")
    private Instant lastInterestPaymentDate;

    @Column(name = "maturity_date")
    private Instant maturityDate;

    @Column(name = "memo_approved_date")
    private Instant memoApprovedDate;

    @Column(name = "next_interest_payment_date")
    private Instant nextInterestPaymentDate;

    @Column(name = "original_issue_date")
    private Instant originalIssueDate;

    @Column(name = "rollover_previous_value_date")
    private Instant rolloverPreviousValueDate;

    @Column(name = "value_date")
    private Instant valueDate;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "is_rollover", nullable = false)
    private boolean rollover;

    @Column(nullable = false)
    private boolean processed;
}
