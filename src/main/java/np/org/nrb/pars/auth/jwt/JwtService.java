package np.org.nrb.pars.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import np.org.nrb.pars.config.ParsSecurityProperties;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final ParsSecurityProperties properties;
    private final SecretKey key;

    public JwtService(ParsSecurityProperties properties) {
        this.properties = properties;
        String secret = properties.getJwt().getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("pars.security.jwt.secret must be configured");
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            // Fail fast: a short secret must never be silently padded into a weak HMAC key.
            throw new IllegalStateException("pars.security.jwt.secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String createAccessToken(UUID userId, String username, UUID sessionId) {
        Instant now = Instant.now();
        Instant exp = now.plus(properties.getJwt().getAccessTokenTtl());
        return Jwts.builder()
                .subject(userId.toString())
                .claim("uname", username)
                .claim("sid", sessionId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
