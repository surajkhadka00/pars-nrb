package np.org.nrb.pars.deal.repository;

import np.org.nrb.pars.deal.model.FiscalYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FiscalYearRepository extends JpaRepository<FiscalYear, UUID>, JpaSpecificationExecutor<FiscalYear> {

    boolean existsByValue(String value);

    @Query("""
            select f from FiscalYear f
            where f.startDate <= :date and f.endDate >= :date
            """)
    List<FiscalYear> findCoveringDate(@Param("date") LocalDate date);

    @Query("""
            select count(f) > 0 from FiscalYear f
            where f.startDate <= :endDate
              and f.endDate >= :startDate
              and (:id is null or f.id <> :id)
            """)
    boolean existsOverlapping(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("id") UUID id
    );
}
