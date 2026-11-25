package np.org.nrb.pars.deal.controller;

import jakarta.validation.Valid;
import np.org.nrb.pars.common.api.ApiMeta;
import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.deal.model.dto.CounterpartyLimitRequest;
import np.org.nrb.pars.deal.model.dto.CounterpartyLimitResponse;
import np.org.nrb.pars.deal.service.CounterpartyLimitService;
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
@RequestMapping("/api/v1/counterparty-limits")
public class CounterpartyLimitController {

    private final CounterpartyLimitService counterpartyLimitService;

    public CounterpartyLimitController(CounterpartyLimitService counterpartyLimitService) {
        this.counterpartyLimitService = counterpartyLimitService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.COUNTERPARTYLIMIT_READ + "')")
    public ApiResponse<List<CounterpartyLimitResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "$filter", required = false) String filter
    ) {
        Page<CounterpartyLimitResponse> result = counterpartyLimitService.list(page, size, sort, filter);
        return ApiResponse.paged(result.getContent(), ApiMeta.from(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.COUNTERPARTYLIMIT_READ + "')")
    public ApiResponse<CounterpartyLimitResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(counterpartyLimitService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.COUNTERPARTYLIMIT_WRITE + "')")
    public ApiResponse<CounterpartyLimitResponse> create(@Valid @RequestBody CounterpartyLimitRequest request) {
        return ApiResponse.ok("Counterparty limit created", counterpartyLimitService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.COUNTERPARTYLIMIT_WRITE + "')")
    public ApiResponse<CounterpartyLimitResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CounterpartyLimitRequest request
    ) {
        return ApiResponse.ok("Counterparty limit updated", counterpartyLimitService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.COUNTERPARTYLIMIT_WRITE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        counterpartyLimitService.delete(id);
        return ApiResponse.ok("Counterparty limit deleted", null);
    }
}
