package np.org.nrb.pars.auth.jwt;

import np.org.nrb.pars.config.ParsSecurityProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    @Test
    void rejectsShortSecretInsteadOfPaddingIt() {
        ParsSecurityProperties properties = new ParsSecurityProperties();
        properties.getJwt().setSecret("short-secret");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> new JwtService(properties));
        assertTrue(error.getMessage().contains("at least 32 bytes"));
    }

    @Test
    void rejectsBlankSecret() {
        ParsSecurityProperties properties = new ParsSecurityProperties();
        properties.getJwt().setSecret("   ");

        assertThrows(IllegalStateException.class, () -> new JwtService(properties));
    }

    @Test
    void acceptsValidSecretAndSignsTokens() {
        ParsSecurityProperties properties = new ParsSecurityProperties();
        properties.getJwt().setSecret("this-is-a-valid-32-byte-secret-key-for-tests");

        JwtService service = new JwtService(properties);
        String token = service.createAccessToken(
                java.util.UUID.randomUUID(), "tester", java.util.UUID.randomUUID());
        assertNotNull(token);
        assertNotNull(service.parse(token));
    }
}
