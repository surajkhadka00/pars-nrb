package np.org.nrb.pars.deal.controller;

import jakarta.validation.Valid;
import np.org.nrb.pars.common.api.ApiMeta;
import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.deal.model.dto.FiscalYearRequest;
import np.org.nrb.pars.deal.model.dto.FiscalYearResponse;
import np.org.nrb.pars.deal.service.FiscalYearService;
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
@RequestMapping("/api/v1/fiscal-years")
public class FiscalYearController {

    private final FiscalYearService fiscalYearService;

    public FiscalYearController(FiscalYearService fiscalYearService) {
        this.fiscalYearService = fiscalYearService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.FISCALYEAR_READ + "')")
    public ApiResponse<List<FiscalYearResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "$filter", required = false) String filter
    ) {
        Page<FiscalYearResponse> result = fiscalYearService.list(page, size, sort, filter);
        return ApiResponse.paged(result.getContent(), ApiMeta.from(result));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAuthority('" + PermissionCodes.FISCALYEAR_READ + "')")
    public ApiResponse<List<OptionItem>> options() {
        return ApiResponse.ok(fiscalYearService.options());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.FISCALYEAR_READ + "')")
    public ApiResponse<FiscalYearResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(fiscalYearService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.FISCALYEAR_WRITE + "')")
    public ApiResponse<FiscalYearResponse> create(@Valid @RequestBody FiscalYearRequest request) {
        return ApiResponse.ok("Fiscal year created", fiscalYearService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.FISCALYEAR_WRITE + "')")
    public ApiResponse<FiscalYearResponse> update(@PathVariable UUID id, @Valid @RequestBody FiscalYearRequest request) {
        return ApiResponse.ok("Fiscal year updated", fiscalYearService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.FISCALYEAR_WRITE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        fiscalYearService.delete(id);
        return ApiResponse.ok("Fiscal year deleted", null);
    }
}
