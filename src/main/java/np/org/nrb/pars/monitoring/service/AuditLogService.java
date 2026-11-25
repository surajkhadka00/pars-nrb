package np.org.nrb.pars.monitoring.service;

import np.org.nrb.pars.common.query.ODataQuery;
import np.org.nrb.pars.monitoring.model.AuditLog;
import np.org.nrb.pars.monitoring.model.dto.AuditLogResponse;
import np.org.nrb.pars.monitoring.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditLogService {

    private final AuditLogRepository repository;
    private final ODataQuery oDataQuery;

    public AuditLogService(AuditLogRepository repository, ODataQuery oDataQuery) {
        this.repository = repository;
        this.oDataQuery = oDataQuery;
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> index(int page, int size, String sort, String filter) {
        Pageable pageable = oDataQuery.pageable(AuditLog.class, page, size, sort);
        return oDataQuery.findAll(repository, AuditLog.class, filter, pageable).map(this::toResponse);
    }

    @Async
    public void record(String method, String path, int status, String payload, long durationMs, UUID userId) {
        AuditLog log = new AuditLog();
        log.setMethod(method);
        log.setPath(path);
        log.setStatus(status);
        log.setPayload(payload);
        log.setDurationMs(durationMs);
        log.setUserId(userId);
        repository.save(log);
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getMethod(),
                log.getPath(),
                log.getStatus(),
                log.getPayload(),
                log.getDurationMs(),
                log.getUserId(),
                log.getCreatedAt()
        );
    }
}
