package np.org.nrb.pars.deal.repository;

import jakarta.persistence.EntityManager;
import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.deal.model.Deal;
import np.org.nrb.pars.deal.model.DealType;
import np.org.nrb.pars.deal.model.FiscalYear;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public class DealRepositoryCustomImpl implements DealRepositoryCustom {

    private final EntityManager entityManager;
    private final FiscalYearRepository fiscalYearRepository;
    private final Clock clock = Clock.system(DealDocumentNumbers.ZONE);

    public DealRepositoryCustomImpl(EntityManager entityManager, FiscalYearRepository fiscalYearRepository) {
        this.entityManager = entityManager;
        this.fiscalYearRepository = fiscalYearRepository;
    }

    @Override
    public void assignDocumentNo(Deal deal) {
        if (deal.getDocumentNo() != null && !deal.getDocumentNo().isBlank()) {
            return;
        }
        DealType dealType = deal.getDealType();
        if (dealType == null || dealType.getValue() == null || dealType.getValue().isBlank()) {
            throw Exceptions.badRequest("Deal type is required to generate the document number");
        }
        LocalDate fiscalDate = DealDocumentNumbers.fiscalDate(deal.getDealDate(), clock);
        FiscalYear fiscalYear = coveringFiscalYear(fiscalDate);
        String prefix = fiscalYear.getValue() + "_" + dealType.getValue() + "_";
        lockSeries(prefix);
        int serial = maxSerial(prefix) + 1;
        deal.setDocumentNo(DealDocumentNumbers.documentNo(fiscalYear.getValue(), dealType.getValue(), serial));
    }

    private FiscalYear coveringFiscalYear(LocalDate date) {
        List<FiscalYear> years = fiscalYearRepository.findCoveringDate(date);
        if (years.isEmpty()) {
            throw Exceptions.badRequest("No fiscal year covers " + date);
        }
        if (years.size() > 1) {
            throw Exceptions.badRequest("More than one fiscal year covers " + date);
        }
        return years.get(0);
    }

    private void lockSeries(String prefix) {
        entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(hashtext(?1)::bigint)")
                .setParameter(1, prefix)
                .getResultList();
    }

    private int maxSerial(String prefix) {
        Number max = (Number) entityManager.createNativeQuery("""
                        SELECT COALESCE(MAX(CAST(substr(document_no, ?1) AS integer)), 0)
                        FROM p_deal
                        WHERE document_no LIKE ?2 ESCAPE '\\'
                          AND substr(document_no, ?1) ~ '^[0-9]+$'
                        """)
                .setParameter(1, DealDocumentNumbers.suffixStart(prefix))
                .setParameter(2, DealDocumentNumbers.likePattern(prefix))
                .getSingleResult();
        return max == null ? 0 : max.intValue();
    }
}
