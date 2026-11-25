package np.org.nrb.pars.deal.controller;

import jakarta.validation.Valid;
import np.org.nrb.pars.common.api.ApiMeta;
import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.deal.model.dto.CounterpartyRequest;
import np.org.nrb.pars.deal.model.dto.CounterpartyResponse;
import np.org.nrb.pars.deal.service.CounterpartyService;
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
@RequestMapping("/api/v1/counterparties")
public class CounterpartyController {

    private final CounterpartyService counterpartyService;

    public CounterpartyController(CounterpartyService counterpartyService) {
        this.counterpartyService = counterpartyService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.COUNTERPARTY_READ + "')")
    public ApiResponse<List<CounterpartyResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "$filter", required = false) String filter
    ) {
        Page<CounterpartyResponse> result = counterpartyService.list(page, size, sort, filter);
        return ApiResponse.paged(result.getContent(), ApiMeta.from(result));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAuthority('" + PermissionCodes.COUNTERPARTY_READ + "')")
    public ApiResponse<List<OptionItem>> options() {
        return ApiResponse.ok(counterpartyService.options());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.COUNTERPARTY_READ + "')")
    public ApiResponse<CounterpartyResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(counterpartyService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.COUNTERPARTY_WRITE + "')")
    public ApiResponse<CounterpartyResponse> create(@Valid @RequestBody CounterpartyRequest request) {
        return ApiResponse.ok("Counterparty created", counterpartyService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.COUNTERPARTY_WRITE + "')")
    public ApiResponse<CounterpartyResponse> update(@PathVariable UUID id, @Valid @RequestBody CounterpartyRequest request) {
        return ApiResponse.ok("Counterparty updated", counterpartyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.COUNTERPARTY_WRITE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        counterpartyService.delete(id);
        return ApiResponse.ok("Counterparty deleted", null);
    }
}
