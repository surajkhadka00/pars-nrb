package np.org.nrb.pars.deal.service;

import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.common.query.ODataQuery;
import np.org.nrb.pars.deal.model.Nostro;
import np.org.nrb.pars.deal.model.dto.NostroRequest;
import np.org.nrb.pars.deal.model.dto.NostroResponse;
import np.org.nrb.pars.deal.repository.NostroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class NostroService {

    private final NostroRepository repository;
    private final DealLookups lookups;
    private final ODataQuery oDataQuery;

    public NostroService(NostroRepository repository, DealLookups lookups, ODataQuery oDataQuery) {
        this.repository = repository;
        this.lookups = lookups;
        this.oDataQuery = oDataQuery;
    }

    @Transactional(readOnly = true)
    public Page<NostroResponse> list(int page, int size, String sort, String filter) {
        Pageable pageable = oDataQuery.pageable(Nostro.class, page, size, sort);
        return oDataQuery.findAll(repository, Nostro.class, filter, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OptionItem> options() {
        return repository.findAll().stream()
                .map(n -> new OptionItem(n.getId(), n.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public NostroResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    public NostroResponse create(NostroRequest request) {
        if (repository.existsByValue(request.value())) {
            throw Exceptions.conflict("Nostro value already exists");
        }
        Nostro entity = new Nostro();
        apply(entity, request);
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public NostroResponse update(UUID id, NostroRequest request) {
        Nostro entity = load(id);
        if (request.version() != null && !Objects.equals(entity.getVersion(), request.version())) {
            throw Exceptions.optimisticLock();
        }
        if (!entity.getValue().equals(request.value()) && repository.existsByValue(request.value())) {
            throw Exceptions.conflict("Nostro value already exists");
        }
        apply(entity, request);
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(load(id));
    }

    private void apply(Nostro entity, NostroRequest request) {
        entity.setCounterparty(lookups.counterparty(request.counterpartyId()));
        entity.setCurrency(lookups.requiredCurrency(request.currencyId()));
        entity.setValue(request.value());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setGlAcNo(request.glAcNo());
        entity.setIban(request.iban());
        entity.setSwiftCode(request.swiftCode());
        entity.setActive(request.active() == null || request.active());
    }

    private Nostro load(UUID id) {
        return repository.findDetailedById(id).orElseThrow(() -> Exceptions.notFound("Nostro not found"));
    }

    private NostroResponse toResponse(Nostro entity) {
        return new NostroResponse(
                entity.getId(),
                entity.getCounterparty() == null ? null : entity.getCounterparty().getId(),
                entity.getCurrency().getId(),
                entity.getCurrency().getIsoCode(),
                entity.getValue(),
                entity.getName(),
                entity.getDescription(),
                entity.getGlAcNo(),
                entity.getIban(),
                entity.getSwiftCode(),
                entity.isActive(),
                entity.getVersion()
        );
    }
}
