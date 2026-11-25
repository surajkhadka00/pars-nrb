package np.org.nrb.pars.deal.repository;

import np.org.nrb.pars.deal.model.DealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface DealTypeRepository extends JpaRepository<DealType, UUID>, JpaSpecificationExecutor<DealType> {
    boolean existsByValue(String value);
}
