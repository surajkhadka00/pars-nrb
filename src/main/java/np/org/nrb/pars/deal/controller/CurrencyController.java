package np.org.nrb.pars.deal.controller;

import jakarta.validation.Valid;
import np.org.nrb.pars.common.api.ApiMeta;
import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.deal.model.dto.CurrencyRequest;
import np.org.nrb.pars.deal.model.dto.CurrencyResponse;
import np.org.nrb.pars.deal.service.CurrencyService;
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
@RequestMapping("/api/v1/currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.CURRENCY_READ + "')")
    public ApiResponse<List<CurrencyResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "$filter", required = false) String filter
    ) {
        Page<CurrencyResponse> result = currencyService.list(page, size, sort, filter);
        return ApiResponse.paged(result.getContent(), ApiMeta.from(result));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAuthority('" + PermissionCodes.CURRENCY_READ + "')")
    public ApiResponse<List<OptionItem>> options() {
        return ApiResponse.ok(currencyService.options());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.CURRENCY_READ + "')")
    public ApiResponse<CurrencyResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(currencyService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.CURRENCY_WRITE + "')")
    public ApiResponse<CurrencyResponse> create(@Valid @RequestBody CurrencyRequest request) {
        return ApiResponse.ok("Currency created", currencyService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.CURRENCY_WRITE + "')")
    public ApiResponse<CurrencyResponse> update(@PathVariable UUID id, @Valid @RequestBody CurrencyRequest request) {
        return ApiResponse.ok("Currency updated", currencyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.CURRENCY_WRITE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        currencyService.delete(id);
        return ApiResponse.ok("Currency deleted", null);
    }
}
