package np.org.nrb.pars.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import np.org.nrb.pars.auth.jwt.JwtService;
import np.org.nrb.pars.auth.service.AuthService;
import np.org.nrb.pars.common.context.CurrentUser;
import np.org.nrb.pars.common.exception.ApiException;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthService authService;

    public JwtAuthenticationFilter(JwtService jwtService, AuthService authService) {
        this.jwtService = jwtService;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtService.parse(token);
                UUID userId = UUID.fromString(claims.getSubject());
                UUID sessionId = UUID.fromString(claims.get("sid", String.class));
                CurrentUser currentUser = authService.loadCurrentUser(userId, sessionId);
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                currentUser.roles().forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
                currentUser.permissions().forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(currentUser, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                MDC.put("userId", currentUser.userId().toString());
                MDC.put("sessionId", currentUser.sessionId().toString());
            } catch (JwtException | IllegalArgumentException | ApiException ex) {
                SecurityContextHolder.clearContext();
            }
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("userId");
            MDC.remove("sessionId");
        }
    }
}
