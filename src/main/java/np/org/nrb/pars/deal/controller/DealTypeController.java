package np.org.nrb.pars.deal.controller;

import jakarta.validation.Valid;
import np.org.nrb.pars.common.api.ApiMeta;
import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.deal.model.dto.DealTypeRequest;
import np.org.nrb.pars.deal.model.dto.DealTypeResponse;
import np.org.nrb.pars.deal.service.DealTypeService;
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
@RequestMapping("/api/v1/deal-types")
public class DealTypeController {

    private final DealTypeService dealTypeService;

    public DealTypeController(DealTypeService dealTypeService) {
        this.dealTypeService = dealTypeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.DEALTYPE_READ + "')")
    public ApiResponse<List<DealTypeResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "$filter", required = false) String filter
    ) {
        Page<DealTypeResponse> result = dealTypeService.list(page, size, sort, filter);
        return ApiResponse.paged(result.getContent(), ApiMeta.from(result));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAuthority('" + PermissionCodes.DEALTYPE_READ + "')")
    public ApiResponse<List<OptionItem>> options() {
        return ApiResponse.ok(dealTypeService.options());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.DEALTYPE_READ + "')")
    public ApiResponse<DealTypeResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(dealTypeService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.DEALTYPE_WRITE + "')")
    public ApiResponse<DealTypeResponse> create(@Valid @RequestBody DealTypeRequest request) {
        return ApiResponse.ok("Deal type created", dealTypeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.DEALTYPE_WRITE + "')")
    public ApiResponse<DealTypeResponse> update(@PathVariable UUID id, @Valid @RequestBody DealTypeRequest request) {
        return ApiResponse.ok("Deal type updated", dealTypeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.DEALTYPE_WRITE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        dealTypeService.delete(id);
        return ApiResponse.ok("Deal type deleted", null);
    }
}
