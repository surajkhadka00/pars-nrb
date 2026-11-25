package np.org.nrb.pars.deal.repository;

import np.org.nrb.pars.deal.model.ExchangeRate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, UUID>, JpaSpecificationExecutor<ExchangeRate> {

    @Override
    @EntityGraph(attributePaths = {"baseCurrency", "currency"})
    Page<ExchangeRate> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"baseCurrency", "currency"})
    Page<ExchangeRate> findAll(Specification<ExchangeRate> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"baseCurrency", "currency"})
    Optional<ExchangeRate> findDetailedById(UUID id);

    boolean existsByBaseCurrencyIdAndCurrencyIdAndRateDate(UUID baseCurrencyId, UUID currencyId, LocalDate rateDate);

    Optional<ExchangeRate> findByBaseCurrencyIdAndCurrencyIdAndRateDate(
            UUID baseCurrencyId,
            UUID currencyId,
            LocalDate rateDate
    );
}
