package np.org.nrb.pars.monitoring.web;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import np.org.nrb.pars.common.context.CurrentUser;
import np.org.nrb.pars.monitoring.service.AuditLogService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Component
public class ApiTimingFilter extends OncePerRequestFilter {

    private static final int PAYLOAD_CACHE_LIMIT = 8192;
    private static final int PAYLOAD_STORE_LIMIT = 4000;
    private static final Set<String> SENSITIVE_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/change-password"
    );
    private static final Pattern SENSITIVE_JSON = Pattern.compile(
            "(?i)(\"(password|refreshToken|accessToken|secret|token)\"\\s*:\\s*\")[^\"]*(\")"
    );

    private final MeterRegistry meterRegistry;
    private final AuditLogService auditLogService;

    public ApiTimingFilter(MeterRegistry meterRegistry, AuditLogService auditLogService) {
        this.meterRegistry = meterRegistry;
        this.auditLogService = auditLogService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingRequestWrapper cachingRequest = wrap(request);
        long start = System.nanoTime();
        try {
            filterChain.doFilter(cachingRequest, response);
        } finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            String path = resolvePath(cachingRequest);
            if (shouldRecord(path)) {
                Timer.builder("pars.api.requests")
                        .tag("method", cachingRequest.getMethod())
                        .tag("status", String.valueOf(response.getStatus()))
                        .tag("uri", path)
                        .register(meterRegistry)
                        .record(durationMs, TimeUnit.MILLISECONDS);
                auditLogService.record(
                        cachingRequest.getMethod(),
                        path,
                        response.getStatus(),
                        extractPayload(cachingRequest, path),
                        durationMs,
                        currentUserId()
                );
            }
        }
    }

    private static ContentCachingRequestWrapper wrap(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper cached) {
            return cached;
        }
        return new ContentCachingRequestWrapper(request, PAYLOAD_CACHE_LIMIT);
    }

    private static String extractPayload(ContentCachingRequestWrapper request, String path) {
        if (SENSITIVE_PATHS.contains(path) || SENSITIVE_PATHS.contains(request.getRequestURI())) {
            return null;
        }
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            return null;
        }
        byte[] body = request.getContentAsByteArray();
        if (body.length == 0) {
            return null;
        }
        Charset charset = StandardCharsets.UTF_8;
        if (request.getCharacterEncoding() != null) {
            charset = Charset.forName(request.getCharacterEncoding());
        }
        String payload = new String(body, charset);
        payload = SENSITIVE_JSON.matcher(payload).replaceAll("$1***$3");
        if (payload.length() > PAYLOAD_STORE_LIMIT) {
            return payload.substring(0, PAYLOAD_STORE_LIMIT);
        }
        return payload;
    }

    private static boolean shouldRecord(String path) {
        if (path == null) {
            return false;
        }
        if (path.startsWith("/actuator") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            return false;
        }
        return !path.endsWith("/options");
    }

    private static String resolvePath(HttpServletRequest request) {
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return pattern == null ? request.getRequestURI() : pattern.toString();
    }

    private static UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CurrentUser currentUser) {
            return currentUser.userId();
        }
        return null;
    }
}
