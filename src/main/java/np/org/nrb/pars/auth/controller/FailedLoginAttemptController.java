package np.org.nrb.pars.auth.controller;

import np.org.nrb.pars.auth.model.dto.FailedAttemptResponse;
import np.org.nrb.pars.auth.service.AuthService;
import np.org.nrb.pars.common.api.ApiMeta;
import np.org.nrb.pars.common.api.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/failed-login-attempts")
public class FailedLoginAttemptController {

    private final AuthService authService;

    public FailedLoginAttemptController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<FailedAttemptResponse>> index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "$filter", required = false) String filter
    ) {
        Page<FailedAttemptResponse> result = authService.indexFailedAttempts(page, size, sort, filter);
        return ApiResponse.paged(result.getContent(), ApiMeta.from(result));
    }
}
