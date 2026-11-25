package np.org.nrb.pars.deal.service;

import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.common.query.ODataQuery;
import np.org.nrb.pars.deal.model.CounterpartyLimit;
import np.org.nrb.pars.deal.model.dto.CounterpartyLimitRequest;
import np.org.nrb.pars.deal.model.dto.CounterpartyLimitResponse;
import np.org.nrb.pars.deal.repository.CounterpartyLimitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class CounterpartyLimitService {

    private final CounterpartyLimitRepository repository;
    private final DealLookups lookups;
    private final ODataQuery oDataQuery;

    public CounterpartyLimitService(
            CounterpartyLimitRepository repository,
            DealLookups lookups,
            ODataQuery oDataQuery
    ) {
        this.repository = repository;
        this.lookups = lookups;
        this.oDataQuery = oDataQuery;
    }

    @Transactional(readOnly = true)
    public Page<CounterpartyLimitResponse> list(int page, int size, String sort, String filter) {
        Pageable pageable = oDataQuery.pageable(CounterpartyLimit.class, page, size, sort);
        return oDataQuery.findAll(repository, CounterpartyLimit.class, filter, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CounterpartyLimitResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    public CounterpartyLimitResponse create(CounterpartyLimitRequest request) {
        CounterpartyLimit entity = new CounterpartyLimit();
        apply(entity, request);
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public CounterpartyLimitResponse update(UUID id, CounterpartyLimitRequest request) {
        CounterpartyLimit entity = load(id);
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

    private void apply(CounterpartyLimit entity, CounterpartyLimitRequest request) {
        entity.setCounterparty(lookups.requiredCounterparty(request.counterpartyId()));
        entity.setDateFrom(request.dateFrom());
        entity.setDateTo(request.dateTo());
        entity.setDescription(request.description());
        entity.setLimitCriteria(request.limitCriteria());
        entity.setLimitType(request.limitType());
        entity.setLimitValue(request.limitValue());
        entity.setActive(request.active() == null || request.active());
    }

    private CounterpartyLimit load(UUID id) {
        return repository.findDetailedById(id).orElseThrow(() -> Exceptions.notFound("Counterparty limit not found"));
    }

    private CounterpartyLimitResponse toResponse(CounterpartyLimit entity) {
        return new CounterpartyLimitResponse(
                entity.getId(),
                entity.getCounterparty().getId(),
                entity.getCounterparty().getName(),
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
