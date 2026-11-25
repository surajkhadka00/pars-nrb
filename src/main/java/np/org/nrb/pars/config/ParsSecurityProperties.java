package np.org.nrb.pars.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "pars.security")
public class ParsSecurityProperties {

    private Jwt jwt = new Jwt();
    private Lockout lockout = new Lockout();
    private Inactivity inactivity = new Inactivity();
    private Cors cors = new Cors();
    private RateLimit rateLimit = new RateLimit();

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public Lockout getLockout() {
        return lockout;
    }

    public void setLockout(Lockout lockout) {
        this.lockout = lockout;
    }

    public Inactivity getInactivity() {
        return inactivity;
    }

    public void setInactivity(Inactivity inactivity) {
        this.inactivity = inactivity;
    }

    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    public static class Jwt {
        private String secret;
        private Duration accessTokenTtl = Duration.ofMinutes(15);
        private Duration refreshTokenTtl = Duration.ofDays(7);

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Duration getAccessTokenTtl() {
            return accessTokenTtl;
        }

        public void setAccessTokenTtl(Duration accessTokenTtl) {
            this.accessTokenTtl = accessTokenTtl;
        }

        public Duration getRefreshTokenTtl() {
            return refreshTokenTtl;
        }

        public void setRefreshTokenTtl(Duration refreshTokenTtl) {
            this.refreshTokenTtl = refreshTokenTtl;
        }
    }

    public static class Lockout {
        private int maxFailedAttempts = 5;
        private Duration lockDuration = Duration.ofMinutes(15);

        public int getMaxFailedAttempts() {
            return maxFailedAttempts;
        }

        public void setMaxFailedAttempts(int maxFailedAttempts) {
            this.maxFailedAttempts = maxFailedAttempts;
        }

        public Duration getLockDuration() {
            return lockDuration;
        }

        public void setLockDuration(Duration lockDuration) {
            this.lockDuration = lockDuration;
        }
    }

    public static class Inactivity {
        private Duration sessionTimeout = Duration.ofMinutes(30);
        private Duration userExpireAfter = Duration.ofDays(90);

        public Duration getSessionTimeout() {
            return sessionTimeout;
        }

        public void setSessionTimeout(Duration sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
        }

        public Duration getUserExpireAfter() {
            return userExpireAfter;
        }

        public void setUserExpireAfter(Duration userExpireAfter) {
            this.userExpireAfter = userExpireAfter;
        }
    }

    public static class Cors {
        private List<String> allowedOrigins;

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class RateLimit {
        private int loginMaxAttempts = 10;
        private Duration loginWindow = Duration.ofMinutes(1);

        public int getLoginMaxAttempts() {
            return loginMaxAttempts;
        }

        public void setLoginMaxAttempts(int loginMaxAttempts) {
            this.loginMaxAttempts = loginMaxAttempts;
        }

        public Duration getLoginWindow() {
            return loginWindow;
        }

        public void setLoginWindow(Duration loginWindow) {
            this.loginWindow = loginWindow;
        }
    }
}
