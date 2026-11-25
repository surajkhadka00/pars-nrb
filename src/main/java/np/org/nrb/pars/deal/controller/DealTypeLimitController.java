package np.org.nrb.pars.deal.controller;

import jakarta.validation.Valid;
import np.org.nrb.pars.common.api.ApiMeta;
import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.deal.model.dto.DealTypeLimitRequest;
import np.org.nrb.pars.deal.model.dto.DealTypeLimitResponse;
import np.org.nrb.pars.deal.service.DealTypeLimitService;
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
@RequestMapping("/api/v1/deal-type-limits")
public class DealTypeLimitController {

    private final DealTypeLimitService dealTypeLimitService;

    public DealTypeLimitController(DealTypeLimitService dealTypeLimitService) {
        this.dealTypeLimitService = dealTypeLimitService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.DEALTYPELIMIT_READ + "')")
    public ApiResponse<List<DealTypeLimitResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "$filter", required = false) String filter
    ) {
        Page<DealTypeLimitResponse> result = dealTypeLimitService.list(page, size, sort, filter);
        return ApiResponse.paged(result.getContent(), ApiMeta.from(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.DEALTYPELIMIT_READ + "')")
    public ApiResponse<DealTypeLimitResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(dealTypeLimitService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.DEALTYPELIMIT_WRITE + "')")
    public ApiResponse<DealTypeLimitResponse> create(@Valid @RequestBody DealTypeLimitRequest request) {
        return ApiResponse.ok("Deal type limit created", dealTypeLimitService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.DEALTYPELIMIT_WRITE + "')")
    public ApiResponse<DealTypeLimitResponse> update(@PathVariable UUID id, @Valid @RequestBody DealTypeLimitRequest request) {
        return ApiResponse.ok("Deal type limit updated", dealTypeLimitService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.DEALTYPELIMIT_WRITE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        dealTypeLimitService.delete(id);
        return ApiResponse.ok("Deal type limit deleted", null);
    }
}
