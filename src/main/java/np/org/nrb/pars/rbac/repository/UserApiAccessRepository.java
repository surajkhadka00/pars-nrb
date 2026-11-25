package np.org.nrb.pars.rbac.repository;

import np.org.nrb.pars.rbac.model.UserApiAccess;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserApiAccessRepository extends JpaRepository<UserApiAccess, UUID> {

    @EntityGraph(attributePaths = "permission")
    List<UserApiAccess> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
