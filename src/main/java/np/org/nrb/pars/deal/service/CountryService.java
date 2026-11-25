package np.org.nrb.pars.deal.service;

import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.common.query.ODataQuery;
import np.org.nrb.pars.deal.model.Country;
import np.org.nrb.pars.deal.model.dto.CountryRequest;
import np.org.nrb.pars.deal.model.dto.CountryResponse;
import np.org.nrb.pars.deal.repository.CountryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class CountryService {

    private final CountryRepository repository;
    private final ODataQuery oDataQuery;

    public CountryService(CountryRepository repository, ODataQuery oDataQuery) {
        this.repository = repository;
        this.oDataQuery = oDataQuery;
    }

    @Transactional(readOnly = true)
    public Page<CountryResponse> list(int page, int size, String sort, String filter) {
        Pageable pageable = oDataQuery.pageable(Country.class, page, size, sort);
        return oDataQuery.findAll(repository, Country.class, filter, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OptionItem> options() {
        return repository.findAll().stream()
                .map(country -> new OptionItem(country.getId(), country.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CountryResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    public CountryResponse create(CountryRequest request) {
        if (repository.existsByValue(request.value())) {
            throw Exceptions.conflict("Country value already exists");
        }
        Country entity = new Country();
        apply(entity, request);
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public CountryResponse update(UUID id, CountryRequest request) {
        Country entity = load(id);
        if (request.version() != null && !Objects.equals(entity.getVersion(), request.version())) {
            throw Exceptions.optimisticLock();
        }
        if (!entity.getValue().equals(request.value()) && repository.existsByValue(request.value())) {
            throw Exceptions.conflict("Country value already exists");
        }
        apply(entity, request);
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(load(id));
    }

    private void apply(Country entity, CountryRequest request) {
        entity.setValue(request.value());
        entity.setName(request.name());
        entity.setActive(request.active() == null || request.active());
    }

    private Country load(UUID id) {
        return repository.findById(id).orElseThrow(() -> Exceptions.notFound("Country not found"));
    }

    private CountryResponse toResponse(Country entity) {
        return new CountryResponse(
                entity.getId(),
                entity.getValue(),
                entity.getName(),
                entity.isActive(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedBy(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }
}
