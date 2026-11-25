package np.org.nrb.pars.auth.repository;

import np.org.nrb.pars.auth.model.AuthSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID>, JpaSpecificationExecutor<AuthSession> {

    @Override
    @EntityGraph(attributePaths = "user")
    Page<AuthSession> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "user")
    Page<AuthSession> findAll(Specification<AuthSession> spec, Pageable pageable);

    Optional<AuthSession> findByRefreshTokenHash(String refreshTokenHash);

    Optional<AuthSession> findByPreviousRefreshTokenHash(String previousRefreshTokenHash);

    List<AuthSession> findByUserIdOrderByIssuedAtDesc(UUID userId);

    List<AuthSession> findByFamilyId(UUID familyId);

    /**
     * Bulk update avoids incrementing @Version on the session entity. Concurrent
     * authenticated requests (e.g. several chat queries) must not fail with an
     * optimistic-lock error just because they all bump "last activity".
     */
    @Modifying
    @Query("update AuthSession s set s.lastActivityAt = :now where s.id = :id")
    int updateLastActivityAt(@Param("id") UUID id, @Param("now") Instant now);

    @Modifying
    @Query("update AuthSession s set s.revokedAt = :now where s.familyId = :familyId and s.revokedAt is null")
    int revokeFamily(@Param("familyId") UUID familyId, @Param("now") Instant now);

    /**
     * Atomic refresh rotation: only one concurrent request can match the current hash.
     * The previous hash is retained so replay of the old token revokes the family.
     */
    @Modifying
    @Query("""
            update AuthSession s set s.previousRefreshTokenHash = s.refreshTokenHash,
                   s.refreshTokenHash = :nextHash, s.lastActivityAt = :now
            where s.id = :id and s.refreshTokenHash = :currentHash
              and s.revokedAt is null and s.expiresAt > :now
            """)
    int rotateRefreshToken(@Param("id") UUID id, @Param("currentHash") String currentHash,
                           @Param("nextHash") String nextHash, @Param("now") Instant now);
}
