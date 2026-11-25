package np.org.nrb.pars.deal.controller;

import jakarta.validation.Valid;
import np.org.nrb.pars.common.api.ApiMeta;
import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.deal.model.dto.InstrumentCodeRequest;
import np.org.nrb.pars.deal.model.dto.InstrumentCodeResponse;
import np.org.nrb.pars.deal.service.InstrumentCodeService;
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
@RequestMapping("/api/v1/instrument-codes")
public class InstrumentCodeController {

    private final InstrumentCodeService instrumentCodeService;

    public InstrumentCodeController(InstrumentCodeService instrumentCodeService) {
        this.instrumentCodeService = instrumentCodeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.INSTRUMENT_READ + "')")
    public ApiResponse<List<InstrumentCodeResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "$filter", required = false) String filter
    ) {
        Page<InstrumentCodeResponse> result = instrumentCodeService.list(page, size, sort, filter);
        return ApiResponse.paged(result.getContent(), ApiMeta.from(result));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAuthority('" + PermissionCodes.INSTRUMENT_READ + "')")
    public ApiResponse<List<OptionItem>> options() {
        return ApiResponse.ok(instrumentCodeService.options());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.INSTRUMENT_READ + "')")
    public ApiResponse<InstrumentCodeResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(instrumentCodeService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.INSTRUMENT_WRITE + "')")
    public ApiResponse<InstrumentCodeResponse> create(@Valid @RequestBody InstrumentCodeRequest request) {
        return ApiResponse.ok("Instrument code created", instrumentCodeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.INSTRUMENT_WRITE + "')")
    public ApiResponse<InstrumentCodeResponse> update(@PathVariable UUID id, @Valid @RequestBody InstrumentCodeRequest request) {
        return ApiResponse.ok("Instrument code updated", instrumentCodeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.INSTRUMENT_WRITE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        instrumentCodeService.delete(id);
        return ApiResponse.ok("Instrument code deleted", null);
    }
}
