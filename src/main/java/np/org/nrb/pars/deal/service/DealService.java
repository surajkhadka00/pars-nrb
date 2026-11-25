package np.org.nrb.pars.deal.service;

import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.common.query.ODataQuery;
import np.org.nrb.pars.deal.model.Counterparty;
import np.org.nrb.pars.deal.model.Currency;
import np.org.nrb.pars.deal.model.Deal;
import np.org.nrb.pars.deal.model.Nostro;
import np.org.nrb.pars.deal.model.dto.DealRequest;
import np.org.nrb.pars.deal.model.dto.DealResponse;
import np.org.nrb.pars.deal.repository.DealRepository;
import np.org.nrb.pars.usermanagement.model.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class DealService {

    private final DealRepository repository;
    private final DealLookups lookups;
    private final ODataQuery oDataQuery;

    public DealService(DealRepository repository, DealLookups lookups, ODataQuery oDataQuery) {
        this.repository = repository;
        this.lookups = lookups;
        this.oDataQuery = oDataQuery;
    }

    @Transactional(readOnly = true)
    public Page<DealResponse> list(int page, int size, String sort, String filter) {
        Pageable pageable = oDataQuery.pageable(Deal.class, page, size, sort);
        return oDataQuery.findAll(repository, Deal.class, filter, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public DealResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    public DealResponse create(DealRequest request) {
        Deal entity = new Deal();
        apply(entity, request);
        repository.assignDocumentNo(entity);
        repository.save(entity);
        return toResponse(load(entity.getId()));
    }

    @Transactional
    public DealResponse update(UUID id, DealRequest request) {
        Deal entity = load(id);
        if (request.version() != null && !Objects.equals(entity.getVersion(), request.version())) {
            throw Exceptions.optimisticLock();
        }
        apply(entity, request);
        repository.save(entity);
        return toResponse(load(entity.getId()));
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(load(id));
    }

    private void apply(Deal entity, DealRequest request) {
        entity.setDealType(lookups.dealType(request.dealTypeId()));
        entity.setInstrumentCode(lookups.instrumentCode(request.instrumentCodeId()));
        entity.setUser(lookups.user(request.userId()));
        entity.setApprovedBy(lookups.user(request.approvedById()));
        entity.setRejectedBy(lookups.user(request.rejectedById()));
        entity.setSubmittedBy(lookups.user(request.submittedById()));
        entity.setVerifiedBy(lookups.user(request.verifiedById()));
        entity.setCurrency(lookups.currency(request.currencyId()));
        entity.setCounterCurrency(lookups.currency(request.counterCurrencyId()));
        entity.setOurNostro(lookups.nostro(request.ourNostroId()));
        entity.setTheirNostro(lookups.nostro(request.theirNostroId()));
        entity.setCounterparty(lookups.counterparty(request.counterpartyId()));
        entity.setIiCounterparty(lookups.counterparty(request.iiCounterpartyId()));
        entity.setIiiCounterparty(lookups.counterparty(request.iiiCounterpartyId()));
        entity.setAllotedAmt(request.allotedAmt());
        entity.setAllotedUnits(request.allotedUnits());
        entity.setAmount(request.amount());
        entity.setBidAmt(request.bidAmt());
        entity.setContraAmt(request.contraAmt());
        entity.setCoupon(request.coupon());
        entity.setDiscountedPricePerUnit(request.discountedPricePerUnit());
        entity.setDiscountedValue(request.discountedValue());
        entity.setEffectiveYield(request.effectiveYield());
        entity.setExchangeRate(request.exchangeRate());
        entity.setIiRate(request.iiRate());
        entity.setIiiRate(request.iiiRate());
        entity.setInterestRate(request.interestRate());
        entity.setPayableAmt(request.payableAmt());
        entity.setRolloverPreviousRate(request.rolloverPreviousRate());
        entity.setRolloverValueAmount(request.rolloverValueAmount());
        entity.setTotalCost(request.totalCost());
        entity.setTotalPayable(request.totalPayable());
        entity.setTenureInDays(request.tenureInDays());
        entity.setBbgCode(request.bbgCode());
        entity.setBondCode(request.bondCode());
        entity.setContactMode(request.contactMode());
        entity.setCusipNo(request.cusipNo());
        entity.setDescription(request.description());
        entity.setDirection(request.direction());
        entity.setDirection2(request.direction2());
        entity.setDocAction(request.docAction() == null ? "CO" : request.docAction());
        entity.setDocStatus(request.docStatus() == null ? "DR" : request.docStatus());
        entity.setHelp(request.help());
        entity.setIiContactMode(request.iiContactMode());
        entity.setIiContactPersonOrCode(request.iiContactPersonOrCode());
        entity.setIiiContactMode(request.iiiContactMode());
        entity.setIiiContactPersonOrCode(request.iiiContactPersonOrCode());
        entity.setInterestPaymentFrequency(request.interestPaymentFrequency());
        entity.setMarketType(request.marketType());
        entity.setMemoRefNo(request.memoRefNo());
        entity.setOurCustodian(request.ourCustodian());
        entity.setTheirCustodian(request.theirCustodian());
        entity.setRejectReason(request.rejectReason());
        entity.setRolloverDealRefNo(request.rolloverDealRefNo());
        entity.setRolloverOption(request.rolloverOption());
        entity.setRolloverRemarks(request.rolloverRemarks());
        entity.setSeries(request.series());
        entity.setTrader(request.trader());
        entity.setBiddingDate(request.biddingDate());
        entity.setDateApproved(request.dateApproved());
        entity.setDateRejected(request.dateRejected());
        entity.setDateSubmitted(request.dateSubmitted());
        entity.setDateVerified(request.dateVerified());
        entity.setDealDate(request.dealDate());
        entity.setDealTime(request.dealTime());
        entity.setLastInterestPaymentDate(request.lastInterestPaymentDate());
        entity.setMaturityDate(request.maturityDate());
        entity.setMemoApprovedDate(request.memoApprovedDate());
        entity.setNextInterestPaymentDate(request.nextInterestPaymentDate());
        entity.setOriginalIssueDate(request.originalIssueDate());
        entity.setRolloverPreviousValueDate(request.rolloverPreviousValueDate());
        entity.setValueDate(request.valueDate());
        entity.setActive(request.active() == null || request.active());
        entity.setRollover(Boolean.TRUE.equals(request.rollover()));
        entity.setProcessed(Boolean.TRUE.equals(request.processed()));
    }

    private Deal load(UUID id) {
        return repository.findWithRefsById(id).orElseThrow(() -> Exceptions.notFound("Deal not found"));
    }

    private DealResponse toResponse(Deal entity) {
        return new DealResponse(
                entity.getId(),
                entity.getDocumentNo(),
                entity.getDealType().getId(),
                entity.getInstrumentCode() == null ? null : entity.getInstrumentCode().getId(),
                id(entity.getUser()),
                id(entity.getApprovedBy()),
                id(entity.getRejectedBy()),
                id(entity.getSubmittedBy()),
                id(entity.getVerifiedBy()),
                id(entity.getCurrency()),
                id(entity.getCounterCurrency()),
                id(entity.getOurNostro()),
                id(entity.getTheirNostro()),
                id(entity.getCounterparty()),
                id(entity.getIiCounterparty()),
                id(entity.getIiiCounterparty()),
                entity.getAllotedAmt(),
                entity.getAllotedUnits(),
                entity.getAmount(),
                entity.getBidAmt(),
                entity.getContraAmt(),
                entity.getCoupon(),
                entity.getDiscountedPricePerUnit(),
                entity.getDiscountedValue(),
                entity.getEffectiveYield(),
                entity.getExchangeRate(),
                entity.getIiRate(),
                entity.getIiiRate(),
                entity.getInterestRate(),
                entity.getPayableAmt(),
                entity.getRolloverPreviousRate(),
                entity.getRolloverValueAmount(),
                entity.getTotalCost(),
                entity.getTotalPayable(),
                entity.getTenureInDays(),
                entity.getBbgCode(),
                entity.getBondCode(),
                entity.getContactMode(),
                entity.getCusipNo(),
                entity.getDescription(),
                entity.getDirection(),
                entity.getDirection2(),
                entity.getDocAction(),
                entity.getDocStatus(),
                entity.getHelp(),
                entity.getIiContactMode(),
                entity.getIiContactPersonOrCode(),
                entity.getIiiContactMode(),
                entity.getIiiContactPersonOrCode(),
                entity.getInterestPaymentFrequency(),
                entity.getMarketType(),
                entity.getMemoRefNo(),
                entity.getOurCustodian(),
                entity.getTheirCustodian(),
                entity.getRejectReason(),
                entity.getRolloverDealRefNo(),
                entity.getRolloverOption(),
                entity.getRolloverRemarks(),
                entity.getSeries(),
                entity.getTrader(),
                entity.getBiddingDate(),
                entity.getDateApproved(),
                entity.getDateRejected(),
                entity.getDateSubmitted(),
                entity.getDateVerified(),
                entity.getDealDate(),
                entity.getDealTime(),
                entity.getLastInterestPaymentDate(),
                entity.getMaturityDate(),
                entity.getMemoApprovedDate(),
                entity.getNextInterestPaymentDate(),
                entity.getOriginalIssueDate(),
                entity.getRolloverPreviousValueDate(),
                entity.getValueDate(),
                entity.isActive(),
                entity.isRollover(),
                entity.isProcessed(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedBy(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    private static UUID id(AppUser user) {
        return user == null ? null : user.getId();
    }

    private static UUID id(Currency currency) {
        return currency == null ? null : currency.getId();
    }

    private static UUID id(Nostro nostro) {
        return nostro == null ? null : nostro.getId();
    }

    private static UUID id(Counterparty counterparty) {
        return counterparty == null ? null : counterparty.getId();
    }
}
