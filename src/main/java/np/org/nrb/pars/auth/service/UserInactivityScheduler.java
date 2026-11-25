package np.org.nrb.pars.auth.service;

import np.org.nrb.pars.auth.model.AuthSession;
import np.org.nrb.pars.auth.repository.AuthSessionRepository;
import np.org.nrb.pars.config.ParsSecurityProperties;
import np.org.nrb.pars.usermanagement.model.AppUser;
import np.org.nrb.pars.usermanagement.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class UserInactivityScheduler {

    private static final Logger log = LoggerFactory.getLogger(UserInactivityScheduler.class);

    private final AppUserRepository userRepository;
    private final AuthSessionRepository sessionRepository;
    private final ParsSecurityProperties properties;

    public UserInactivityScheduler(
            AppUserRepository userRepository,
            AuthSessionRepository sessionRepository,
            ParsSecurityProperties properties
    ) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.properties = properties;
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Kathmandu")
    @Transactional
    public void expireInactiveUsers() {
        Instant cutoff = Instant.now().minus(properties.getInactivity().getUserExpireAfter());
        List<AppUser> inactive = userRepository.findInactiveSince(cutoff);
        Instant now = Instant.now();
        for (AppUser user : inactive) {
            user.setExpired(true);
            user.setEnabled(false);
            user.setInactiveExpireAt(now);
            for (AuthSession session : sessionRepository.findByUserIdOrderByIssuedAtDesc(user.getId())) {
                if (!session.isRevoked()) {
                    session.setRevokedAt(now);
                }
            }
        }
        if (!inactive.isEmpty()) {
            log.info("Expired {} inactive users", inactive.size());
        }
    }
}
