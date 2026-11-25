package np.org.nrb.pars.deal.controller;

import jakarta.validation.Valid;
import np.org.nrb.pars.common.api.ApiMeta;
import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.deal.model.dto.DealRequest;
import np.org.nrb.pars.deal.model.dto.DealResponse;
import np.org.nrb.pars.deal.service.DealService;
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
@RequestMapping("/api/v1/deals")
public class DealController {

    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.DEAL_READ + "')")
    public ApiResponse<List<DealResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "$filter", required = false) String filter
    ) {
        Page<DealResponse> result = dealService.list(page, size, sort, filter);
        return ApiResponse.paged(result.getContent(), ApiMeta.from(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.DEAL_READ + "')")
    public ApiResponse<DealResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(dealService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.DEAL_WRITE + "')")
    public ApiResponse<DealResponse> create(@Valid @RequestBody DealRequest request) {
        return ApiResponse.ok("Deal created", dealService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.DEAL_WRITE + "')")
    public ApiResponse<DealResponse> update(@PathVariable UUID id, @Valid @RequestBody DealRequest request) {
        return ApiResponse.ok("Deal updated", dealService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.DEAL_WRITE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        dealService.delete(id);
        return ApiResponse.ok("Deal deleted", null);
    }
}
