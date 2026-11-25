package np.org.nrb.pars.deal.service;

import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.common.query.ODataQuery;
import np.org.nrb.pars.deal.model.InstrumentCode;
import np.org.nrb.pars.deal.model.dto.InstrumentCodeRequest;
import np.org.nrb.pars.deal.model.dto.InstrumentCodeResponse;
import np.org.nrb.pars.deal.repository.InstrumentCodeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class InstrumentCodeService {

    private final InstrumentCodeRepository repository;
    private final DealLookups lookups;
    private final ODataQuery oDataQuery;

    public InstrumentCodeService(InstrumentCodeRepository repository, DealLookups lookups, ODataQuery oDataQuery) {
        this.repository = repository;
        this.lookups = lookups;
        this.oDataQuery = oDataQuery;
    }

    @Transactional(readOnly = true)
    public Page<InstrumentCodeResponse> list(int page, int size, String sort, String filter) {
        Pageable pageable = oDataQuery.pageable(InstrumentCode.class, page, size, sort);
        return oDataQuery.findAll(repository, InstrumentCode.class, filter, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OptionItem> options() {
        return repository.findAll().stream()
                .map(i -> new OptionItem(i.getId(), i.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public InstrumentCodeResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    public InstrumentCodeResponse create(InstrumentCodeRequest request) {
        if (repository.existsByValue(request.value())) {
            throw Exceptions.conflict("Instrument code value already exists");
        }
        InstrumentCode entity = new InstrumentCode();
        apply(entity, request);
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public InstrumentCodeResponse update(UUID id, InstrumentCodeRequest request) {
        InstrumentCode entity = load(id);
        if (request.version() != null && !Objects.equals(entity.getVersion(), request.version())) {
            throw Exceptions.optimisticLock();
        }
        if (!entity.getValue().equals(request.value()) && repository.existsByValue(request.value())) {
            throw Exceptions.conflict("Instrument code value already exists");
        }
        apply(entity, request);
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(load(id));
    }

    private void apply(InstrumentCode entity, InstrumentCodeRequest request) {
        entity.setDealType(lookups.dealType(request.dealTypeId()));
        entity.setValue(request.value());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setActive(request.active() == null || request.active());
    }

    private InstrumentCode load(UUID id) {
        return repository.findDetailedById(id).orElseThrow(() -> Exceptions.notFound("Instrument code not found"));
    }

    private InstrumentCodeResponse toResponse(InstrumentCode entity) {
        return new InstrumentCodeResponse(
                entity.getId(),
                entity.getDealType().getId(),
                entity.getDealType().getName(),
                entity.getValue(),
                entity.getName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getVersion()
        );
    }
}
