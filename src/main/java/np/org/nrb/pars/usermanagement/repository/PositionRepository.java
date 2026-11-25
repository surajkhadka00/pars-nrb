package np.org.nrb.pars.usermanagement.repository;

import np.org.nrb.pars.usermanagement.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface PositionRepository extends JpaRepository<Position, UUID>, JpaSpecificationExecutor<Position> {
    boolean existsByValue(String value);
}
