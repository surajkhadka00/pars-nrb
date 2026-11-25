package np.org.nrb.pars.usermanagement.service;

import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.common.query.ODataQuery;
import np.org.nrb.pars.usermanagement.model.Position;
import np.org.nrb.pars.usermanagement.model.dto.PositionRequest;
import np.org.nrb.pars.usermanagement.model.dto.PositionResponse;
import np.org.nrb.pars.usermanagement.repository.PositionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class PositionService {

    private final PositionRepository repository;
    private final ODataQuery oDataQuery;

    public PositionService(PositionRepository repository, ODataQuery oDataQuery) {
        this.repository = repository;
        this.oDataQuery = oDataQuery;
    }

    @Transactional(readOnly = true)
    public Page<PositionResponse> list(int page, int size, String sort, String filter) {
        Pageable pageable = oDataQuery.pageable(Position.class, page, size, sort);
        return oDataQuery.findAll(repository, Position.class, filter, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OptionItem> options() {
        return repository.findAll().stream()
                .map(position -> new OptionItem(position.getId(), position.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PositionResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    public PositionResponse create(PositionRequest request) {
        if (repository.existsByValue(request.value())) {
            throw Exceptions.conflict("Position value already exists");
        }
        Position entity = new Position();
        apply(entity, request);
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public PositionResponse update(UUID id, PositionRequest request) {
        Position entity = load(id);
        if (request.version() != null && !Objects.equals(entity.getVersion(), request.version())) {
            throw Exceptions.optimisticLock();
        }
        if (!entity.getValue().equals(request.value()) && repository.existsByValue(request.value())) {
            throw Exceptions.conflict("Position value already exists");
        }
        apply(entity, request);
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(load(id));
    }

    private void apply(Position entity, PositionRequest request) {
        entity.setValue(request.value());
        entity.setName(request.name());
        entity.setLimitType(blankToNull(request.limitType()));
        entity.setLimitValue(request.limitValue());
        entity.setActive(request.active() == null || request.active());
    }

    private Position load(UUID id) {
        return repository.findById(id).orElseThrow(() -> Exceptions.notFound("Position not found"));
    }

    private PositionResponse toResponse(Position entity) {
        return new PositionResponse(
                entity.getId(),
                entity.getValue(),
                entity.getName(),
                entity.getLimitType(),
                entity.getLimitValue(),
                entity.isActive(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedBy(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
