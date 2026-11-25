package np.org.nrb.pars.nrb.controller;

import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.nrb.domain.NrbForexViews.ForexDayView;
import np.org.nrb.pars.nrb.service.NrbForexService;
import np.org.nrb.pars.rbac.model.PermissionCodes;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * PARS application endpoints for official NRB forex data. These are OUR endpoints —
 * the only NRB-provided endpoint is GET /api/forex/v1/rates.
 */
@RestController
public class NrbForexController {

    private final NrbForexService service;

    public NrbForexController(NrbForexService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/nrb/forex/latest")
    @PreAuthorize("hasAuthority('AI_FOREX_READ')")
    public ApiResponse<ForexDayView> latest() {
        return ApiResponse.ok(service.latest());
    }

    @GetMapping("/api/v1/nrb/forex")
    @PreAuthorize("hasAuthority('AI_FOREX_READ')")
    public ApiResponse<List<ForexDayView>> historical(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.ok(service.historical(from, to));
    }

    @GetMapping("/api/v1/nrb/forex/{currencyCode}")
    @PreAuthorize("hasAuthority('AI_FOREX_READ')")
    public ApiResponse<ForexDayView> byCurrency(
            @PathVariable String currencyCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(service.byCurrency(currencyCode, date));
    }

    @PostMapping("/api/v1/admin/nrb/forex/sync")
    @PreAuthorize("hasAuthority('AI_SOURCE_MANAGE')")
    public ApiResponse<Map<String, Object>> sync(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        int saved = service.sync(from, to);
        return ApiResponse.ok(Map.of("savedDays", saved));
    }
}
