package np.org.nrb.pars.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import np.org.nrb.pars.auth.jwt.JwtService;
import np.org.nrb.pars.auth.model.AuthFailedAttempt;
import np.org.nrb.pars.auth.model.AuthSession;
import np.org.nrb.pars.auth.model.dto.AuthSessionResponse;
import np.org.nrb.pars.auth.model.dto.FailedAttemptResponse;
import np.org.nrb.pars.auth.model.dto.LoginRequest;
import np.org.nrb.pars.auth.model.dto.MeResponse;
import np.org.nrb.pars.auth.model.dto.RegisterRequest;
import np.org.nrb.pars.auth.model.dto.SessionResponse;
import np.org.nrb.pars.auth.model.dto.TokenResponse;
import np.org.nrb.pars.auth.repository.AuthFailedAttemptRepository;
import np.org.nrb.pars.auth.repository.AuthSessionRepository;
import np.org.nrb.pars.common.context.CurrentUser;
import np.org.nrb.pars.common.query.ODataQuery;
import np.org.nrb.pars.common.exception.ApiException;
import np.org.nrb.pars.common.exception.ApiException;
import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.config.ParsSecurityProperties;
import np.org.nrb.pars.rbac.model.Role;
import np.org.nrb.pars.rbac.repository.RoleRepository;
import np.org.nrb.pars.rbac.service.PermissionResolver;
import np.org.nrb.pars.usermanagement.model.AppUser;
import np.org.nrb.pars.usermanagement.repository.AppUserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthSessionRepository sessionRepository;
    private final AuthFailedAttemptRepository failedAttemptRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PermissionResolver permissionResolver;
    private final ParsSecurityProperties properties;
    private final ODataQuery oDataQuery;
    private final EntityManager entityManager;

    public AuthService(
            AppUserRepository userRepository,
            RoleRepository roleRepository,
            AuthSessionRepository sessionRepository,
            AuthFailedAttemptRepository failedAttemptRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            PermissionResolver permissionResolver,
            ParsSecurityProperties properties,
            ODataQuery oDataQuery,
            EntityManager entityManager
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.sessionRepository = sessionRepository;
        this.failedAttemptRepository = failedAttemptRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.permissionResolver = permissionResolver;
        this.properties = properties;
        this.oDataQuery = oDataQuery;
        this.entityManager = entityManager;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request, HttpServletRequest http) {
        if (userRepository.existsByUsername(request.username())) {
            throw Exceptions.conflict("Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw Exceptions.conflict("Email already exists");
        }
        Role userRole = roleRepository.findByCode("USER")
                .orElseThrow(() -> Exceptions.notFound("USER role is not configured"));
        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPasswordChangedAt(Instant.now());
        user.getRoles().add(userRole);
        userRepository.save(user);
        return login(new LoginRequest(request.username(), request.password()), http);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public TokenResponse login(LoginRequest request, HttpServletRequest http) {
        Instant now = Instant.now();
        AppUser user = userRepository.findWithRolesByUsername(request.username()).orElse(null);
        if (user == null) {
            recordFailure(request.username(), http, "UNKNOWN_USER");
            throw Exceptions.unauthorized("Invalid username or password");
        }
        if (user.isExpired()) {
            recordFailure(request.username(), http, "EXPIRED");
            throw Exceptions.userExpired("User account has expired due to inactivity");
        }
        if (!user.isEnabled()) {
            recordFailure(request.username(), http, "DISABLED");
            throw Exceptions.forbidden("User account is disabled");
        }
        if (user.isLocked(now)) {
            recordFailure(request.username(), http, "LOCKED");
            throw Exceptions.locked("Account is locked until " + user.getLockedUntil());
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            handleFailedPassword(user, http);
            throw Exceptions.unauthorized("Invalid username or password");
        }
        user.setFailedCount(0);
        user.setLockedUntil(null);
        user.setLockedAt(null);
        user.setLastSuccessfulLoginAt(now);
        user.setInactiveExpireAt(now.plus(properties.getInactivity().getUserExpireAfter()));
        userRepository.save(user);
        String refresh = refreshTokenService.generate();
        AuthSession session = new AuthSession();
        session.setUser(user);
        session.setFamilyId(UUID.randomUUID());
        session.setRefreshTokenHash(refreshTokenService.hash(refresh));
        session.setIpAddress(clientIp(http));
        session.setUserAgent(userAgent(http));
        session.setIssuedAt(now);
        session.setLastActivityAt(now);
        session.setExpiresAt(now.plus(properties.getJwt().getRefreshTokenTtl()));
        sessionRepository.save(session);
        return tokens(user, session, refresh);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public TokenResponse refresh(String refreshToken, HttpServletRequest http) {
        Instant now = Instant.now();
        String hash = refreshTokenService.hash(refreshToken);
        AuthSession reused = sessionRepository.findByPreviousRefreshTokenHash(hash).orElse(null);
        if (reused != null && !reused.isRevoked()) {
            revokeFamily(reused.getFamilyId(), now);
            throw Exceptions.unauthorized("Refresh token reuse detected");
        }
        AuthSession session = sessionRepository.findByRefreshTokenHash(hash)
                .orElseThrow(() -> Exceptions.unauthorized("Invalid refresh token"));
        if (session.isRevoked() || session.isExpired(now)) {
            throw Exceptions.unauthorized("Refresh token is no longer valid");
        }
        AppUser user = userRepository.findWithRolesById(session.getUser().getId())
                .orElseThrow(() -> Exceptions.unauthorized("User not found"));
        assertUserActive(user, now);
        String nextRefresh = refreshTokenService.generate();
        String nextHash = refreshTokenService.hash(nextRefresh);
        int rotated = sessionRepository.rotateRefreshToken(session.getId(), hash, nextHash, now);
        if (rotated != 1) {
            throw Exceptions.unauthorized("Refresh token is no longer valid");
        }
        // The bulk update bypassed @Version; detach the stale managed entity so it
        // cannot overwrite the rotated hashes when the transaction commits.
        entityManager.detach(session);
        return tokens(user, session, nextRefresh);
    }

    @Transactional
    public void logout(CurrentUser currentUser) {
        Instant now = Instant.now();
        AuthSession session = sessionRepository.findById(currentUser.sessionId())
                .orElseThrow(() -> Exceptions.notFound("Session not found"));
        session.setRevokedAt(now);
        sessionRepository.save(session);
    }

    @Transactional
    public void logoutAll(CurrentUser currentUser) {
        Instant now = Instant.now();
        for (AuthSession session : sessionRepository.findByUserIdOrderByIssuedAtDesc(currentUser.userId())) {
            if (!session.isRevoked()) {
                session.setRevokedAt(now);
            }
        }
    }

    @Transactional(readOnly = true)
    public MeResponse me(CurrentUser currentUser) {
        AppUser user = userRepository.findWithRolesById(currentUser.userId())
                .orElseThrow(() -> Exceptions.notFound("User not found"));
        return new MeResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                currentUser.sessionId(),
                currentUser.roles(),
                currentUser.permissions(),
                user.getLastSuccessfulLoginAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<FailedAttemptResponse> indexFailedAttempts(int page, int size, String sort, String filter) {
        Pageable pageable = oDataQuery.pageable(AuthFailedAttempt.class, page, size, sort);
        return oDataQuery.findAll(failedAttemptRepository, AuthFailedAttempt.class, filter, pageable)
                .map(item -> new FailedAttemptResponse(
                        item.getId(),
                        item.getUsername(),
                        item.getIpAddress(),
                        item.getAttemptedAt(),
                        item.getReason()
                ));
    }

    @Transactional(readOnly = true)
    public Page<AuthSessionResponse> indexSessions(int page, int size, String sort, String filter) {
        Pageable pageable = oDataQuery.pageable(AuthSession.class, page, size, sort);
        return oDataQuery.findAll(sessionRepository, AuthSession.class, filter, pageable)
                .map(this::toAdminResponse);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> listSessions(CurrentUser currentUser) {
        return sessionRepository.findByUserIdOrderByIssuedAtDesc(currentUser.userId()).stream()
                .map(session -> toResponse(session, currentUser.sessionId()))
                .toList();
    }

    @Transactional
    public void revokeOwnSession(CurrentUser currentUser, UUID sessionId) {
        AuthSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> Exceptions.notFound("Session not found"));
        if (!session.getUser().getId().equals(currentUser.userId())) {
            throw Exceptions.forbidden("Cannot revoke another user's session");
        }
        session.setRevokedAt(Instant.now());
        sessionRepository.save(session);
    }

    @Transactional
    public void revokeSessionAdmin(UUID sessionId) {
        AuthSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> Exceptions.notFound("Session not found"));
        session.setRevokedAt(Instant.now());
        sessionRepository.save(session);
    }

    @Transactional
    public CurrentUser loadCurrentUser(UUID userId, UUID sessionId) {
        Instant now = Instant.now();
        AuthSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> Exceptions.unauthorized("Session not found"));
        if (session.isRevoked() || session.isExpired(now)) {
            throw Exceptions.unauthorized("Session is no longer valid");
        }
        if (session.getLastActivityAt().isBefore(now.minus(properties.getInactivity().getSessionTimeout()))) {
            session.setRevokedAt(now);
            sessionRepository.save(session);
            throw Exceptions.unauthorized("Session expired due to inactivity");
        }
        AppUser user = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> Exceptions.unauthorized("User not found"));
        if (!session.getUser().getId().equals(user.getId())) {
            throw Exceptions.unauthorized("Session does not match user");
        }
        assertUserActive(user, now);
        if (session.getLastActivityAt().isBefore(now.minusSeconds(30))) {
            // Bulk update: no @Version bump, so parallel requests cannot conflict.
            sessionRepository.updateLastActivityAt(session.getId(), now);
        }
        Set<String> roles = permissionResolver.roleCodes(user);
        Set<String> permissions = permissionResolver.resolve(user);
        return new CurrentUser(user.getId(), user.getUsername(), session.getId(), roles, permissions);
    }

    private void assertUserActive(AppUser user, Instant now) {
        if (user.isExpired()) {
            throw Exceptions.userExpired("User account has expired due to inactivity");
        }
        if (!user.isEnabled()) {
            throw Exceptions.forbidden("User account is disabled");
        }
        if (user.isLocked(now)) {
            throw Exceptions.locked("Account is locked");
        }
    }

    private void handleFailedPassword(AppUser user, HttpServletRequest http) {
        recordFailure(user.getUsername(), http, "BAD_PASSWORD");
        int next = user.getFailedCount() + 1;
        user.setFailedCount(next);
        if (next >= properties.getLockout().getMaxFailedAttempts()) {
            Instant until = Instant.now().plus(properties.getLockout().getLockDuration());
            user.setLockedAt(Instant.now());
            user.setLockedUntil(until);
            user.setFailedCount(0);
        }
        userRepository.save(user);
    }

    private void recordFailure(String username, HttpServletRequest http, String reason) {
        AuthFailedAttempt attempt = new AuthFailedAttempt();
        attempt.setUsername(username);
        attempt.setIpAddress(clientIp(http));
        attempt.setAttemptedAt(Instant.now());
        attempt.setReason(reason);
        failedAttemptRepository.save(attempt);
    }

    private void revokeFamily(UUID familyId, Instant now) {
        sessionRepository.revokeFamily(familyId, now);
    }

    private TokenResponse tokens(AppUser user, AuthSession session, String refreshToken) {
        String access = jwtService.createAccessToken(user.getId(), user.getUsername(), session.getId());
        return new TokenResponse(
                access,
                refreshToken,
                "Bearer",
                properties.getJwt().getAccessTokenTtl().toSeconds()
        );
    }

    private AuthSessionResponse toAdminResponse(AuthSession session) {
        return new AuthSessionResponse(
                session.getId(),
                session.getUser().getId(),
                session.getUser().getUsername(),
                session.getFamilyId(),
                session.getIpAddress(),
                session.getUserAgent(),
                session.getIssuedAt(),
                session.getLastActivityAt(),
                session.getExpiresAt(),
                session.getRevokedAt()
        );
    }

    private SessionResponse toResponse(AuthSession session, UUID currentSessionId) {
        return new SessionResponse(
                session.getId(),
                session.getFamilyId(),
                session.getIpAddress(),
                session.getUserAgent(),
                session.getIssuedAt(),
                session.getLastActivityAt(),
                session.getExpiresAt(),
                session.getRevokedAt(),
                session.getId().equals(currentSessionId)
        );
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static String userAgent(HttpServletRequest request) {
        String value = request.getHeader("User-Agent");
        if (value == null) {
            return null;
        }
        return value.length() > 512 ? value.substring(0, 512) : value;
    }
}
