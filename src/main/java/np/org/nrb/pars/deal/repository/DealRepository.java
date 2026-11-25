package np.org.nrb.pars.deal.repository;

import np.org.nrb.pars.deal.model.Deal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface DealRepository extends JpaRepository<Deal, UUID>, JpaSpecificationExecutor<Deal>, DealRepositoryCustom {

    @Override
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {
            "dealType", "instrumentCode", "user", "approvedBy", "rejectedBy", "submittedBy", "verifiedBy",
            "currency", "counterCurrency", "ourNostro", "theirNostro",
            "counterparty", "iiCounterparty", "iiiCounterparty"
    })
    Page<Deal> findAll(Pageable pageable);

    @Override
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {
            "dealType", "instrumentCode", "user", "approvedBy", "rejectedBy", "submittedBy", "verifiedBy",
            "currency", "counterCurrency", "ourNostro", "theirNostro",
            "counterparty", "iiCounterparty", "iiiCounterparty"
    })
    Page<Deal> findAll(Specification<Deal> spec, Pageable pageable);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {
            "dealType", "instrumentCode", "user", "approvedBy", "rejectedBy", "submittedBy", "verifiedBy",
            "currency", "counterCurrency", "ourNostro", "theirNostro",
            "counterparty", "iiCounterparty", "iiiCounterparty"
    })
    Optional<Deal> findWithRefsById(UUID id);
}
