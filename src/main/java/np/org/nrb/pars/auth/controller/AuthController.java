package np.org.nrb.pars.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import np.org.nrb.pars.auth.model.AuthFailedAttempt;
import np.org.nrb.pars.auth.model.dto.FailedAttemptResponse;
import np.org.nrb.pars.auth.model.dto.LoginRequest;
import np.org.nrb.pars.auth.model.dto.MeResponse;
import np.org.nrb.pars.auth.model.dto.RefreshRequest;
import np.org.nrb.pars.auth.model.dto.RegisterRequest;
import np.org.nrb.pars.auth.model.dto.SessionResponse;
import np.org.nrb.pars.auth.model.dto.TokenResponse;
import np.org.nrb.pars.auth.repository.AuthFailedAttemptRepository;
import np.org.nrb.pars.auth.service.AuthService;
import np.org.nrb.pars.common.api.ApiMeta;
import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.common.context.CurrentUser;
import np.org.nrb.pars.common.context.CurrentUserProvider;
import np.org.nrb.pars.common.query.ODataQuery;
import np.org.nrb.pars.rbac.model.PermissionCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final CurrentUserProvider currentUserProvider;
    private final AuthFailedAttemptRepository failedAttemptRepository;
    private final ODataQuery oDataQuery;

    public AuthController(
            AuthService authService,
            CurrentUserProvider currentUserProvider,
            AuthFailedAttemptRepository failedAttemptRepository,
            ODataQuery oDataQuery
    ) {
        this.authService = authService;
        this.currentUserProvider = currentUserProvider;
        this.failedAttemptRepository = failedAttemptRepository;
        this.oDataQuery = oDataQuery;
    }

    @PostMapping("/register")
    public ApiResponse<TokenResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http) {
        return ApiResponse.ok("Registered", authService.register(request, http));
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        log.info("Login attempt for username: {}", request.username());
        return ApiResponse.ok("Logged in", authService.login(request, http));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request, HttpServletRequest http) {
        return ApiResponse.ok(authService.refresh(request.refreshToken(), http));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout(currentUserProvider.require());
        return ApiResponse.ok("Logged out", null);
    }

    @PostMapping("/logout-all")
    public ApiResponse<Void> logoutAll() {
        authService.logoutAll(currentUserProvider.require());
        return ApiResponse.ok("All sessions revoked", null);
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me() {
        return ApiResponse.ok(authService.me(currentUserProvider.require()));
    }

    @GetMapping("/sessions")
    public ApiResponse<List<SessionResponse>> sessions() {
        return ApiResponse.ok(authService.listSessions(currentUserProvider.require()));
    }

    @DeleteMapping("/sessions/{id}")
    public ApiResponse<Void> revokeOwn(@PathVariable UUID id) {
        authService.revokeOwnSession(currentUserProvider.require(), id);
        return ApiResponse.ok("Session revoked", null);
    }

    @DeleteMapping("/admin/sessions/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.SESSION_REVOKE + "')")
    public ApiResponse<Void> revokeAdmin(@PathVariable UUID id) {
        authService.revokeSessionAdmin(id);
        return ApiResponse.ok("Session revoked", null);
    }

    @GetMapping("/failed-attempts")
    @PreAuthorize("hasAuthority('" + PermissionCodes.FAILED_ATTEMPT_READ + "')")
    public ApiResponse<List<FailedAttemptResponse>> failedAttempts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "$filter", required = false) String filter
    ) {
        Page<AuthFailedAttempt> result = oDataQuery.findAll(
                failedAttemptRepository,
                AuthFailedAttempt.class,
                filter,
                oDataQuery.pageable(AuthFailedAttempt.class, page, size, sort)
        );
        List<FailedAttemptResponse> data = result.getContent().stream()
                .map(item -> new FailedAttemptResponse(
                        item.getId(), item.getUsername(), item.getIpAddress(), item.getAttemptedAt(), item.getReason()))
                .toList();
        return ApiResponse.paged(data, ApiMeta.from(result));
    }
}
