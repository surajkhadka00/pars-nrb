package np.org.nrb.pars.deal.controller;

import jakarta.validation.Valid;
import np.org.nrb.pars.common.api.ApiMeta;
import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.deal.model.dto.NostroRequest;
import np.org.nrb.pars.deal.model.dto.NostroResponse;
import np.org.nrb.pars.deal.service.NostroService;
import np.org.nrb.pars.rbac.model.PermissionCodes;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/nostros")
public class NostroController {

    private final NostroService nostroService;

    public NostroController(NostroService nostroService) {
        this.nostroService = nostroService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.NOSTRO_READ + "')")
    public ApiResponse<List<NostroResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "$filter", required = false) String filter
    ) {
        Page<NostroResponse> result = nostroService.list(page, size, sort, filter);
        return ApiResponse.paged(result.getContent(), ApiMeta.from(result));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAuthority('" + PermissionCodes.NOSTRO_READ + "')")
    public ApiResponse<List<OptionItem>> options() {
        return ApiResponse.ok(nostroService.options());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.NOSTRO_READ + "')")
    public ApiResponse<NostroResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(nostroService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.NOSTRO_WRITE + "')")
    public ApiResponse<NostroResponse> create(@Valid @RequestBody NostroRequest request) {
        return ApiResponse.ok("Nostro created", nostroService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.NOSTRO_WRITE + "')")
    public ApiResponse<NostroResponse> update(@PathVariable UUID id, @Valid @RequestBody NostroRequest request) {
        return ApiResponse.ok("Nostro updated", nostroService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.NOSTRO_WRITE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        nostroService.delete(id);
        return ApiResponse.ok("Nostro deleted", null);
    }
}
