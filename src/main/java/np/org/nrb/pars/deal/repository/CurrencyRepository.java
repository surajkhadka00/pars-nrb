package np.org.nrb.pars.deal.repository;

import np.org.nrb.pars.deal.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CurrencyRepository extends JpaRepository<Currency, UUID>, JpaSpecificationExecutor<Currency> {
    boolean existsByIsoCode(String isoCode);

    Optional<Currency> findByIsoCodeIgnoreCase(String isoCode);

    Optional<Currency> findByDescriptionIgnoreCase(String description);
}
