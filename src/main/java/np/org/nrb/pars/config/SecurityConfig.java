package np.org.nrb.pars.config;

import np.org.nrb.pars.auth.security.JsonAccessDeniedHandler;
import np.org.nrb.pars.auth.security.JsonAuthEntryPoint;
import np.org.nrb.pars.auth.security.JwtAuthenticationFilter;
import np.org.nrb.pars.auth.security.LoginRateLimitFilter;
import np.org.nrb.pars.common.web.TraceIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain managementSecurityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JsonAuthEntryPoint authEntryPoint,
            JsonAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
            .securityMatcher("/actuator/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JsonAuthEntryPoint authEntryPoint,
            JsonAccessDeniedHandler accessDeniedHandler,
            LoginRateLimitFilter loginRateLimitFilter,
            TraceIdFilter traceIdFilter
    ) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .headers(headers -> headers
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(frame -> frame.deny())
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                .addHeaderWriter(new StaticHeadersWriter("Content-Security-Policy",
                        "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self'; frame-ancestors 'none'; form-action 'self'; base-uri 'self'; object-src 'none'"))
                .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy",
                        "camera=(), microphone=(), geolocation=(), payment=(), usb=()"))
            )
            .authorizeHttpRequests(auth -> auth
                // .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").denyAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/forgot-password").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/register",
                        "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password").permitAll()
                .requestMatchers(
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-ui",
                        "/swagger-ui/",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-ui.html/**").permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .addFilterBefore(traceIdFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(loginRateLimitFilter, TraceIdFilter.class)
            .addFilterAfter(jwtAuthenticationFilter, LoginRateLimitFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(ParsSecurityProperties securityProperties) {
        CorsConfiguration configuration = corsConfiguration(securityProperties.getCors().getAllowedOrigins());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Fail closed: no configured origins means no cross-origin access. Credentials are
     * enabled only for an explicit allow-list, never for a reflected wildcard.
     */
    static CorsConfiguration corsConfiguration(List<String> configuredOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = normalizeOrigins(configuredOrigins);
        if (origins.isEmpty()) {
            configuration.setAllowedOrigins(List.of());
            configuration.setAllowCredentials(false);
        } else {
            configuration.setAllowedOriginPatterns(origins);
            configuration.setAllowCredentials(true);
        }
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        return configuration;
    }

    private static List<String> normalizeOrigins(List<String> configuredOrigins) {
        if (configuredOrigins == null) {
            return List.of();
        }
        return configuredOrigins.stream()
                .filter(Objects::nonNull)
                .flatMap(origin -> Arrays.stream(origin.split(",")))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .filter(origin -> !"*".equals(origin))
                .distinct()
                .toList();
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> disableJwtAuthenticationFilterDoubleFilter(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>(jwtAuthenticationFilter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<LoginRateLimitFilter> disableLoginRateLimitFilterDoubleFilter(
            LoginRateLimitFilter loginRateLimitFilter) {
        FilterRegistrationBean<LoginRateLimitFilter> registrationBean = new FilterRegistrationBean<>(loginRateLimitFilter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<TraceIdFilter> disableTraceIdDoubleFilter(TraceIdFilter traceIdFilter) {
        FilterRegistrationBean<TraceIdFilter> registrationBean = new FilterRegistrationBean<>(traceIdFilter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }
}
