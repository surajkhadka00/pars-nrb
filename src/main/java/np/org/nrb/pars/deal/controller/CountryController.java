package np.org.nrb.pars.deal.controller;

import jakarta.validation.Valid;
import np.org.nrb.pars.common.api.ApiMeta;
import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.deal.model.dto.CountryRequest;
import np.org.nrb.pars.deal.model.dto.CountryResponse;
import np.org.nrb.pars.deal.service.CountryService;
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
@RequestMapping("/api/v1/countries")
public class CountryController {

    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.COUNTRY_READ + "')")
    public ApiResponse<List<CountryResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "$filter", required = false) String filter
    ) {
        Page<CountryResponse> result = countryService.list(page, size, sort, filter);
        return ApiResponse.paged(result.getContent(), ApiMeta.from(result));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAuthority('" + PermissionCodes.COUNTRY_READ + "')")
    public ApiResponse<List<OptionItem>> options() {
        return ApiResponse.ok(countryService.options());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.COUNTRY_READ + "')")
    public ApiResponse<CountryResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(countryService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.COUNTRY_WRITE + "')")
    public ApiResponse<CountryResponse> create(@Valid @RequestBody CountryRequest request) {
        return ApiResponse.ok("Country created", countryService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.COUNTRY_WRITE + "')")
    public ApiResponse<CountryResponse> update(@PathVariable UUID id, @Valid @RequestBody CountryRequest request) {
        return ApiResponse.ok("Country updated", countryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.COUNTRY_WRITE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        countryService.delete(id);
        return ApiResponse.ok("Country deleted", null);
    }
}
