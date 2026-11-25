package np.org.nrb.pars.deal.repository;

import np.org.nrb.pars.deal.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CountryRepository extends JpaRepository<Country, UUID>, JpaSpecificationExecutor<Country> {
    boolean existsByValue(String value);
}
