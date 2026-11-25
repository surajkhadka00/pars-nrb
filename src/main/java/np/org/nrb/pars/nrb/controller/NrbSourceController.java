package np.org.nrb.pars.nrb.controller;

import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.nrb.repository.NrbSourceRepository;
import np.org.nrb.pars.nrb.service.NrbSourceService;
import np.org.nrb.pars.rbac.model.PermissionCodes;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/nrb")
public class NrbSourceController {

    private final NrbSourceService service;

    public NrbSourceController(NrbSourceService service) {
        this.service = service;
    }

    @GetMapping("/sources")
    @PreAuthorize("hasAuthority('AI_SOURCE_READ')")
    public ApiResponse<List<NrbSourceRepository.SourceRow>> sources() {
        return ApiResponse.ok(service.sources());
    }

    @GetMapping("/sources/{id}")
    @PreAuthorize("hasAuthority('AI_SOURCE_READ')")
    public ApiResponse<NrbSourceRepository.SourceRow> source(@PathVariable UUID id) {
        return ApiResponse.ok(service.sources().stream()
                .filter(s -> s.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("NRB source not found")));
    }

    @GetMapping("/documents")
    @PreAuthorize("hasAuthority('AI_DOCUMENT_READ')")
    public ApiResponse<List<NrbSourceRepository.DocumentRow>> documents(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.documents(page, size));
    }

    @GetMapping("/documents/{id}")
    @PreAuthorize("hasAuthority('AI_DOCUMENT_READ')")
    public ApiResponse<NrbSourceRepository.DocumentRow> document(@PathVariable UUID id) {
        return ApiResponse.ok(service.documents(0, 1000).stream()
                .filter(d -> d.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("NRB document not found")));
    }

    @GetMapping("/sources/status")
    @PreAuthorize("hasAuthority('AI_SOURCE_READ')")
    public ApiResponse<Map<String, Object>> status() {
        return ApiResponse.ok(service.status());
    }

    @PatchMapping("/sources/{id}")
    @PreAuthorize("hasAuthority('AI_SOURCE_MANAGE')")
    public ApiResponse<Void> setEnabled(@PathVariable UUID id, @RequestParam boolean enabled) {
        service.setEnabled(id, enabled);
        return ApiResponse.ok(null);
    }
}
