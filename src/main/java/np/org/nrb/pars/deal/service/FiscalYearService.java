package np.org.nrb.pars.deal.service;

import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.common.query.ODataQuery;
import np.org.nrb.pars.deal.model.FiscalYear;
import np.org.nrb.pars.deal.model.dto.FiscalYearRequest;
import np.org.nrb.pars.deal.model.dto.FiscalYearResponse;
import np.org.nrb.pars.deal.repository.FiscalYearRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class FiscalYearService {

    private final FiscalYearRepository repository;
    private final ODataQuery oDataQuery;

    public FiscalYearService(FiscalYearRepository repository, ODataQuery oDataQuery) {
        this.repository = repository;
        this.oDataQuery = oDataQuery;
    }

    @Transactional(readOnly = true)
    public Page<FiscalYearResponse> list(int page, int size, String sort, String filter) {
        Pageable pageable = oDataQuery.pageable(FiscalYear.class, page, size, sort);
        return oDataQuery.findAll(repository, FiscalYear.class, filter, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OptionItem> options() {
        return repository.findAll().stream()
                .map(year -> new OptionItem(year.getId(), year.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public FiscalYearResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    public FiscalYearResponse create(FiscalYearRequest request) {
        if (repository.existsByValue(request.value())) {
            throw Exceptions.conflict("Fiscal year value already exists");
        }
        validateDates(request, null);
        FiscalYear entity = new FiscalYear();
        apply(entity, request);
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public FiscalYearResponse update(UUID id, FiscalYearRequest request) {
        FiscalYear entity = load(id);
        if (request.version() != null && !Objects.equals(entity.getVersion(), request.version())) {
            throw Exceptions.optimisticLock();
        }
        if (!entity.getValue().equals(request.value()) && repository.existsByValue(request.value())) {
            throw Exceptions.conflict("Fiscal year value already exists");
        }
        validateDates(request, id);
        apply(entity, request);
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(load(id));
    }

    private void validateDates(FiscalYearRequest request, UUID id) {
        if (request.endDate().isBefore(request.startDate())) {
            throw Exceptions.badRequest("End date must be on or after the start date");
        }
        if (repository.existsOverlapping(request.startDate(), request.endDate(), id)) {
            throw Exceptions.conflict("Fiscal year dates overlap an existing fiscal year");
        }
    }

    private void apply(FiscalYear entity, FiscalYearRequest request) {
        entity.setValue(request.value());
        entity.setName(request.name());
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
    }

    private FiscalYear load(UUID id) {
        return repository.findById(id).orElseThrow(() -> Exceptions.notFound("Fiscal year not found"));
    }

    private FiscalYearResponse toResponse(FiscalYear entity) {
        return new FiscalYearResponse(
                entity.getId(),
                entity.getValue(),
                entity.getName(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getCreatedBy(),
                entity.getUpdatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }
}
