package np.org.nrb.pars.deal.repository;

import np.org.nrb.pars.deal.model.InstrumentCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface InstrumentCodeRepository extends JpaRepository<InstrumentCode, UUID>, JpaSpecificationExecutor<InstrumentCode> {
    boolean existsByValue(String value);

    @Override
    @EntityGraph(attributePaths = "dealType")
    Page<InstrumentCode> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "dealType")
    Page<InstrumentCode> findAll(Specification<InstrumentCode> spec, Pageable pageable);

    @EntityGraph(attributePaths = "dealType")
    Optional<InstrumentCode> findDetailedById(UUID id);
}
