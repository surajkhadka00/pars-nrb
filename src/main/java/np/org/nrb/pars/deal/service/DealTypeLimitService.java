package np.org.nrb.pars.deal.service;

import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.common.query.ODataQuery;
import np.org.nrb.pars.deal.model.DealTypeLimit;
import np.org.nrb.pars.deal.model.dto.DealTypeLimitRequest;
import np.org.nrb.pars.deal.model.dto.DealTypeLimitResponse;
import np.org.nrb.pars.deal.repository.DealTypeLimitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class DealTypeLimitService {

    private final DealTypeLimitRepository repository;
    private final DealLookups lookups;
    private final ODataQuery oDataQuery;

    public DealTypeLimitService(DealTypeLimitRepository repository, DealLookups lookups, ODataQuery oDataQuery) {
        this.repository = repository;
        this.lookups = lookups;
        this.oDataQuery = oDataQuery;
    }

    @Transactional(readOnly = true)
    public Page<DealTypeLimitResponse> list(int page, int size, String sort, String filter) {
        Pageable pageable = oDataQuery.pageable(DealTypeLimit.class, page, size, sort);
        return oDataQuery.findAll(repository, DealTypeLimit.class, filter, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public DealTypeLimitResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    public DealTypeLimitResponse create(DealTypeLimitRequest request) {
        DealTypeLimit entity = new DealTypeLimit();
        apply(entity, request);
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public DealTypeLimitResponse update(UUID id, DealTypeLimitRequest request) {
        DealTypeLimit entity = load(id);
        if (request.version() != null && !Objects.equals(entity.getVersion(), request.version())) {
            throw Exceptions.optimisticLock();
        }
        apply(entity, request);
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(load(id));
    }

    private void apply(DealTypeLimit entity, DealTypeLimitRequest request) {
        entity.setDealType(lookups.dealType(request.dealTypeId()));
        entity.setCurrency(lookups.currency(request.currencyId()));
        entity.setDateFrom(request.dateFrom());
        entity.setDateTo(request.dateTo());
        entity.setDescription(request.description());
        entity.setLimitCriteria(request.limitCriteria());
        entity.setLimitType(request.limitType());
        entity.setLimitValue(request.limitValue());
        entity.setActive(request.active() == null || request.active());
    }

    private DealTypeLimit load(UUID id) {
        return repository.findDetailedById(id).orElseThrow(() -> Exceptions.notFound("Deal type limit not found"));
    }

    private DealTypeLimitResponse toResponse(DealTypeLimit entity) {
        return new DealTypeLimitResponse(
                entity.getId(),
                entity.getDealType().getId(),
                entity.getDealType().getName(),
                entity.getCurrency() == null ? null : entity.getCurrency().getId(),
                entity.getCurrency() == null ? null : entity.getCurrency().getIsoCode(),
                entity.getDateFrom(),
                entity.getDateTo(),
                entity.getDescription(),
                entity.getLimitCriteria(),
                entity.getLimitType(),
                entity.getLimitValue(),
                entity.isActive(),
                entity.getVersion()
        );
    }
}
