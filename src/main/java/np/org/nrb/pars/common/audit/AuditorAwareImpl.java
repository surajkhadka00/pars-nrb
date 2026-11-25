package np.org.nrb.pars.common.audit;

import np.org.nrb.pars.common.context.CurrentUserProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AuditorAwareImpl implements AuditorAware<UUID> {

    private final CurrentUserProvider currentUserProvider;

    public AuditorAwareImpl(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public Optional<UUID> getCurrentAuditor() {
        return currentUserProvider.find().map(user -> user.userId());
    }
}
