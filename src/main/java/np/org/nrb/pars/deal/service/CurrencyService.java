package np.org.nrb.pars.deal.service;

import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.common.query.ODataQuery;
import np.org.nrb.pars.deal.model.Currency;
import np.org.nrb.pars.deal.model.dto.CurrencyRequest;
import np.org.nrb.pars.deal.model.dto.CurrencyResponse;
import np.org.nrb.pars.deal.repository.CurrencyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class CurrencyService {

    private final CurrencyRepository repository;
    private final ODataQuery oDataQuery;

    public CurrencyService(CurrencyRepository repository, ODataQuery oDataQuery) {
        this.repository = repository;
        this.oDataQuery = oDataQuery;
    }

    @Transactional(readOnly = true)
    public Page<CurrencyResponse> list(int page, int size, String sort, String filter) {
        Pageable pageable = oDataQuery.pageable(Currency.class, page, size, sort);
        return oDataQuery.findAll(repository, Currency.class, filter, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OptionItem> options() {
        return repository.findAll().stream()
                .map(c -> new OptionItem(c.getId(), c.getIsoCode() + " - " + c.getDescription()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CurrencyResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    public CurrencyResponse create(CurrencyRequest request) {
        if (repository.existsByIsoCode(request.isoCode())) {
            throw Exceptions.conflict("ISO code already exists");
        }
        Currency entity = new Currency();
        apply(entity, request);
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public CurrencyResponse update(UUID id, CurrencyRequest request) {
        Currency entity = load(id);
        if (request.version() != null && !Objects.equals(entity.getVersion(), request.version())) {
            throw Exceptions.optimisticLock();
        }
        if (!entity.getIsoCode().equals(request.isoCode()) && repository.existsByIsoCode(request.isoCode())) {
            throw Exceptions.conflict("ISO code already exists");
        }
        apply(entity, request);
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(load(id));
    }

    private void apply(Currency entity, CurrencyRequest request) {
        entity.setIsoCode(request.isoCode());
        entity.setCurSymbol(request.curSymbol());
        entity.setDescription(request.description());
        entity.setStdPrecision(request.stdPrecision() == null ? 2 : request.stdPrecision());
        entity.setCostingPrecision(request.costingPrecision() == null ? 4 : request.costingPrecision());
        entity.setEuro(Boolean.TRUE.equals(request.euro()));
        entity.setEmuMember(Boolean.TRUE.equals(request.emuMember()));
        entity.setEmuEntryDate(request.emuEntryDate());
        entity.setEmuRate(request.emuRate());
        entity.setRoundOffFactor(request.roundOffFactor());
        entity.setActive(request.active() == null || request.active());
    }

    private Currency load(UUID id) {
        return repository.findById(id).orElseThrow(() -> Exceptions.notFound("Currency not found"));
    }

    private CurrencyResponse toResponse(Currency entity) {
        return new CurrencyResponse(
                entity.getId(),
                entity.getIsoCode(),
                entity.getCurSymbol(),
                entity.getDescription(),
                entity.getStdPrecision(),
                entity.getCostingPrecision(),
                entity.isEuro(),
                entity.isEmuMember(),
                entity.getEmuEntryDate(),
                entity.getEmuRate(),
                entity.getRoundOffFactor(),
                entity.isActive(),
                entity.getVersion()
        );
    }
}
