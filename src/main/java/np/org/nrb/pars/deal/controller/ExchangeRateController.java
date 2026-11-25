package np.org.nrb.pars.deal.controller;

import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.deal.model.dto.ExchangeRateImportResponse;
import np.org.nrb.pars.deal.service.ExchangeRateService;
import np.org.nrb.pars.rbac.model.PermissionCodes;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exchange-rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('" + PermissionCodes.CURRENCY_WRITE + "')")
    public ApiResponse<ExchangeRateImportResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate rateDate
    ) {
        ExchangeRateImportResponse result = exchangeRateService.importExcel(file, rateDate);
        return ApiResponse.ok("Exchange rates imported", result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.CURRENCY_WRITE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        exchangeRateService.delete(id);
        return ApiResponse.ok("Exchange rate deleted", null);
    }
}
