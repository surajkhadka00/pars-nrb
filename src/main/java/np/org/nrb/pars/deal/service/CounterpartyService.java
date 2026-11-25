package np.org.nrb.pars.deal.service;

import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.common.query.ODataQuery;
import np.org.nrb.pars.deal.model.Counterparty;
import np.org.nrb.pars.deal.model.dto.CounterpartyRequest;
import np.org.nrb.pars.deal.model.dto.CounterpartyResponse;
import np.org.nrb.pars.deal.repository.CounterpartyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class CounterpartyService {

    private final CounterpartyRepository repository;
    private final DealLookups lookups;
    private final ODataQuery oDataQuery;

    public CounterpartyService(CounterpartyRepository repository, DealLookups lookups, ODataQuery oDataQuery) {
        this.repository = repository;
        this.lookups = lookups;
        this.oDataQuery = oDataQuery;
    }

    @Transactional(readOnly = true)
    public Page<CounterpartyResponse> list(int page, int size, String sort, String filter) {
        Pageable pageable = oDataQuery.pageable(Counterparty.class, page, size, sort);
        return oDataQuery.findAll(repository, Counterparty.class, filter, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OptionItem> options() {
        return repository.findAll().stream()
                .map(c -> new OptionItem(c.getId(), c.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CounterpartyResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    public CounterpartyResponse create(CounterpartyRequest request) {
        if (repository.existsByValue(request.value())) {
            throw Exceptions.conflict("Counterparty value already exists");
        }
        Counterparty entity = new Counterparty();
        apply(entity, request);
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public CounterpartyResponse update(UUID id, CounterpartyRequest request) {
        Counterparty entity = load(id);
        if (request.version() != null && !Objects.equals(entity.getVersion(), request.version())) {
            throw Exceptions.optimisticLock();
        }
        if (!entity.getValue().equals(request.value()) && repository.existsByValue(request.value())) {
            throw Exceptions.conflict("Counterparty value already exists");
        }
        apply(entity, request);
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(load(id));
    }

    private void apply(Counterparty entity, CounterpartyRequest request) {
        entity.setValue(request.value());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setAddress(request.address());
        entity.setUser(lookups.user(request.userId()));
        entity.setBloombergCode(request.bloombergCode());
        entity.setCountryId(request.countryId());
        entity.setComments(request.comments());
        entity.setContactName(request.contactName());
        entity.setEmail(request.email());
        entity.setInvestmentGrade(request.investmentGrade());
        entity.setOwnBank(Boolean.TRUE.equals(request.ownBank()));
        entity.setLimitType(request.limitType());
        entity.setLimitValue(request.limitValue());
        entity.setLocation(request.location());
        entity.setPhone(request.phone());
        entity.setPosition(request.position());
        entity.setReutersDealingCode(request.reutersDealingCode());
        entity.setActive(request.active() == null || request.active());
    }

    private Counterparty load(UUID id) {
        return repository.findDetailedById(id).orElseThrow(() -> Exceptions.notFound("Counterparty not found"));
    }

    private CounterpartyResponse toResponse(Counterparty entity) {
        return new CounterpartyResponse(
                entity.getId(),
                entity.getValue(),
                entity.getName(),
                entity.getDescription(),
                entity.getAddress(),
                entity.getUser() == null ? null : entity.getUser().getId(),
                entity.getBloombergCode(),
                entity.getCountryId(),
                entity.getComments(),
                entity.getContactName(),
                entity.getEmail(),
                entity.getInvestmentGrade(),
                entity.isOwnBank(),
                entity.getLimitType(),
                entity.getLimitValue(),
                entity.getLocation(),
                entity.getPhone(),
                entity.getPosition(),
                entity.getReutersDealingCode(),
                entity.isActive(),
                entity.getVersion()
        );
    }
}
