package np.org.nrb.pars.deal.repository;

import np.org.nrb.pars.deal.model.DealTypeLimit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface DealTypeLimitRepository extends JpaRepository<DealTypeLimit, UUID>, JpaSpecificationExecutor<DealTypeLimit> {

    @Override
    @EntityGraph(attributePaths = {"dealType", "currency"})
    Page<DealTypeLimit> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"dealType", "currency"})
    Page<DealTypeLimit> findAll(Specification<DealTypeLimit> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"dealType", "currency"})
    Optional<DealTypeLimit> findDetailedById(UUID id);
}
