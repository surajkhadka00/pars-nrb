package np.org.nrb.pars.auth.repository;

import np.org.nrb.pars.auth.model.AuthFailedAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AuthFailedAttemptRepository extends JpaRepository<AuthFailedAttempt, UUID>, JpaSpecificationExecutor<AuthFailedAttempt> {
}
