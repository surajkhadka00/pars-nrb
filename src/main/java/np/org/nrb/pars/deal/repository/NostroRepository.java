package np.org.nrb.pars.deal.repository;

import np.org.nrb.pars.deal.model.Nostro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface NostroRepository extends JpaRepository<Nostro, UUID>, JpaSpecificationExecutor<Nostro> {
    boolean existsByValue(String value);

    @Override
    @EntityGraph(attributePaths = {"currency", "counterparty"})
    Page<Nostro> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"currency", "counterparty"})
    Page<Nostro> findAll(Specification<Nostro> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"currency", "counterparty"})
    Optional<Nostro> findDetailedById(UUID id);
}
