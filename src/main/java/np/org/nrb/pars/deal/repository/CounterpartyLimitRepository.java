package np.org.nrb.pars.deal.repository;

import np.org.nrb.pars.deal.model.CounterpartyLimit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CounterpartyLimitRepository extends JpaRepository<CounterpartyLimit, UUID>, JpaSpecificationExecutor<CounterpartyLimit> {

    @Override
    @EntityGraph(attributePaths = "counterparty")
    Page<CounterpartyLimit> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "counterparty")
    Page<CounterpartyLimit> findAll(Specification<CounterpartyLimit> spec, Pageable pageable);

    @EntityGraph(attributePaths = "counterparty")
    Optional<CounterpartyLimit> findDetailedById(UUID id);
}
