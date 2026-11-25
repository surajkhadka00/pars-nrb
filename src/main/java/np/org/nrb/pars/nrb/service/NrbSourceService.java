package np.org.nrb.pars.nrb.service;

import np.org.nrb.pars.nrb.repository.NrbSourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NrbSourceService {

    private final NrbSourceRepository repository;

    public NrbSourceService(NrbSourceRepository repository) {
        this.repository = repository;
    }

    public List<NrbSourceRepository.SourceRow> sources() {
        return repository.listSources();
    }

    public List<NrbSourceRepository.DocumentRow> documents(int page, int size) {
        return repository.listDocuments(page, size);
    }

    public void setEnabled(UUID id, boolean enabled) {
        if (repository.findSource(id).isEmpty()) {
            throw new IllegalArgumentException("NRB source not found");
        }
        repository.setEnabled(id, enabled);
    }

    public Map<String, Object> status() {
        List<NrbSourceRepository.SourceRow> sources = repository.listSources();
        long enabled = sources.stream().filter(NrbSourceRepository.SourceRow::enabled).count();
        long synced = sources.stream().filter(s -> "SYNCED".equals(s.status())).count();
        return Map.of(
                "sources", sources.size(),
                "enabledSources", enabled,
                "syncedSources", synced,
                "status", synced > 0 ? "HEALTHY" : "NOT_SYNCED");
    }
}
