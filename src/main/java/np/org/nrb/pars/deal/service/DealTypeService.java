package np.org.nrb.pars.deal.service;

import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.common.query.ODataQuery;
import np.org.nrb.pars.deal.model.DealType;
import np.org.nrb.pars.deal.model.dto.DealTypeRequest;
import np.org.nrb.pars.deal.model.dto.DealTypeResponse;
import np.org.nrb.pars.deal.repository.DealTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class DealTypeService {

    private final DealTypeRepository repository;
    private final ODataQuery oDataQuery;

    public DealTypeService(DealTypeRepository repository, ODataQuery oDataQuery) {
        this.repository = repository;
        this.oDataQuery = oDataQuery;
    }

    @Transactional(readOnly = true)
    public Page<DealTypeResponse> list(int page, int size, String sort, String filter) {
        Pageable pageable = oDataQuery.pageable(DealType.class, page, size, sort);
        return oDataQuery.findAll(repository, DealType.class, filter, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OptionItem> options() {
        return repository.findAll().stream()
                .map(t -> new OptionItem(t.getId(), t.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public DealTypeResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    public DealTypeResponse create(DealTypeRequest request) {
        if (repository.existsByValue(request.value())) {
            throw Exceptions.conflict("Deal type value already exists");
        }
        DealType entity = new DealType();
        apply(entity, request);
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public DealTypeResponse update(UUID id, DealTypeRequest request) {
        DealType entity = load(id);
        if (request.version() != null && !Objects.equals(entity.getVersion(), request.version())) {
            throw Exceptions.optimisticLock();
        }
        if (!entity.getValue().equals(request.value()) && repository.existsByValue(request.value())) {
            throw Exceptions.conflict("Deal type value already exists");
        }
        apply(entity, request);
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(load(id));
    }

    private void apply(DealType entity, DealTypeRequest request) {
        entity.setValue(request.value());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setContactMode(request.contactMode());
        entity.setInstrumentCodeApplicable(request.instrumentCodeApplicable() == null || request.instrumentCodeApplicable());
        entity.setLimitType(request.limitType());
        entity.setLimitValue(request.limitValue());
        entity.setActive(request.active() == null || request.active());
    }

    private DealType load(UUID id) {
        return repository.findById(id).orElseThrow(() -> Exceptions.notFound("Deal type not found"));
    }

    private DealTypeResponse toResponse(DealType entity) {
        return new DealTypeResponse(
                entity.getId(),
                entity.getValue(),
                entity.getName(),
                entity.getDescription(),
                entity.getContactMode(),
                entity.isInstrumentCodeApplicable(),
                entity.getLimitType(),
                entity.getLimitValue(),
                entity.isActive(),
                entity.getVersion()
        );
    }
}
