package np.org.nrb.pars.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.cors.CorsConfiguration;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigCorsTest {

    @Test
    void wildcardOriginIsRejected() {
        CorsConfiguration configuration = SecurityConfig.corsConfiguration(List.of("*"));

        assertThat(configuration.checkOrigin("https://phnx.pars.com.np")).isNull();
        assertThat(configuration.getAllowCredentials()).isFalse();
    }

    @Test
    void viteDevOriginIsAllowed() {
        // Regression test for the login 403: the React/Vite dev server runs on
        // http://localhost:5173. The dev profile must allow this explicit origin.
        CorsConfiguration configuration = SecurityConfig.corsConfiguration(
                List.of("http://localhost:5173"));

        assertThat(configuration.checkOrigin("http://localhost:5173"))
                .isEqualTo("http://localhost:5173");
        assertThat(configuration.checkOrigin("http://localhost:3000")).isNull();
        assertThat(configuration.getAllowCredentials()).isTrue();
    }

    @Test
    void devProfileDefaultsToTheViteOrigin() throws IOException {
        List<PropertySource<?>> propertySources = new YamlPropertySourceLoader()
                .load("application-dev", new ClassPathResource("application-dev.yml"));

        assertThat(propertySources.get(0).getProperty("pars.security.cors.allowed-origins"))
                .isEqualTo("${PARS_CORS_ORIGINS:http://localhost:5173}");
    }

    @Test
    void stagingOriginAllowsOnlyThatOrigin() {
        CorsConfiguration configuration = SecurityConfig.corsConfiguration(
                List.of("https://phnx.pars.com.np"));

        assertThat(configuration.checkOrigin("https://phnx.pars.com.np"))
                .isEqualTo("https://phnx.pars.com.np");
        assertThat(configuration.checkOrigin("https://other.example")).isNull();
    }

    @Test
    void blankOriginListFailsClosed() {
        CorsConfiguration configuration = SecurityConfig.corsConfiguration(List.of("  "));

        assertThat(configuration.checkOrigin("http://localhost:8114")).isNull();
        assertThat(configuration.getAllowCredentials()).isFalse();
    }

    @Test
    void patchMethodIsAllowedForSourceAdministration() {
        CorsConfiguration configuration = SecurityConfig.corsConfiguration(List.of("http://localhost:5173"));
        assertThat(configuration.getAllowedMethods()).contains("PATCH", "GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
