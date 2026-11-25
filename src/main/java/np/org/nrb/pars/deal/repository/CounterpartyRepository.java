package np.org.nrb.pars.deal.repository;

import np.org.nrb.pars.deal.model.Counterparty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CounterpartyRepository extends JpaRepository<Counterparty, UUID>, JpaSpecificationExecutor<Counterparty> {
    boolean existsByValue(String value);

    @Override
    @EntityGraph(attributePaths = "user")
    Page<Counterparty> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "user")
    Page<Counterparty> findAll(Specification<Counterparty> spec, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    Optional<Counterparty> findDetailedById(UUID id);
}
