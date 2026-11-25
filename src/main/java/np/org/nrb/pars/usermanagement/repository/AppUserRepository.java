package np.org.nrb.pars.usermanagement.repository;

import np.org.nrb.pars.usermanagement.model.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID>, JpaSpecificationExecutor<AppUser> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"roles", "roles.permissions", "position"})
    @Query("select u from AppUser u where u.username = :username")
    Optional<AppUser> findWithRolesByUsername(@Param("username") String username);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"roles", "roles.permissions", "position"})
    @Query("select u from AppUser u where u.id = :id")
    Optional<AppUser> findWithRolesById(@Param("id") UUID id);

    @Query("select u from AppUser u where u.enabled = true and u.expired = false "
            + "and u.lastSuccessfulLoginAt is not null and u.lastSuccessfulLoginAt < :cutoff")
    List<AppUser> findInactiveSince(@Param("cutoff") Instant cutoff);
}
