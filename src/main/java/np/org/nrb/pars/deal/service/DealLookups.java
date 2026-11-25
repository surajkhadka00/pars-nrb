package np.org.nrb.pars.deal.service;

import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.deal.model.Counterparty;
import np.org.nrb.pars.deal.model.Currency;
import np.org.nrb.pars.deal.model.DealType;
import np.org.nrb.pars.deal.model.InstrumentCode;
import np.org.nrb.pars.deal.model.Nostro;
import np.org.nrb.pars.deal.repository.CounterpartyRepository;
import np.org.nrb.pars.deal.repository.CurrencyRepository;
import np.org.nrb.pars.deal.repository.DealTypeRepository;
import np.org.nrb.pars.deal.repository.InstrumentCodeRepository;
import np.org.nrb.pars.deal.repository.NostroRepository;
import np.org.nrb.pars.usermanagement.model.AppUser;
import np.org.nrb.pars.usermanagement.repository.AppUserRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DealLookups {

    private final DealTypeRepository dealTypeRepository;
    private final InstrumentCodeRepository instrumentCodeRepository;
    private final CurrencyRepository currencyRepository;
    private final NostroRepository nostroRepository;
    private final CounterpartyRepository counterpartyRepository;
    private final AppUserRepository userRepository;

    public DealLookups(
            DealTypeRepository dealTypeRepository,
            InstrumentCodeRepository instrumentCodeRepository,
            CurrencyRepository currencyRepository,
            NostroRepository nostroRepository,
            CounterpartyRepository counterpartyRepository,
            AppUserRepository userRepository
    ) {
        this.dealTypeRepository = dealTypeRepository;
        this.instrumentCodeRepository = instrumentCodeRepository;
        this.currencyRepository = currencyRepository;
        this.nostroRepository = nostroRepository;
        this.counterpartyRepository = counterpartyRepository;
        this.userRepository = userRepository;
    }

    public DealType dealType(UUID id) {
        return dealTypeRepository.findById(id).orElseThrow(() -> Exceptions.notFound("Deal type not found"));
    }

    public InstrumentCode instrumentCode(UUID id) {
        if (id == null) {
            return null;
        }
        return instrumentCodeRepository.findById(id).orElseThrow(() -> Exceptions.notFound("Instrument code not found"));
    }

    public Currency currency(UUID id) {
        if (id == null) {
            return null;
        }
        return currencyRepository.findById(id).orElseThrow(() -> Exceptions.notFound("Currency not found"));
    }

    public Currency requiredCurrency(UUID id) {
        return currencyRepository.findById(id).orElseThrow(() -> Exceptions.notFound("Currency not found"));
    }

    public Nostro nostro(UUID id) {
        if (id == null) {
            return null;
        }
        return nostroRepository.findById(id).orElseThrow(() -> Exceptions.notFound("Nostro not found"));
    }

    public AppUser user(UUID id) {
        if (id == null) {
            return null;
        }
        return userRepository.findById(id).orElseThrow(() -> Exceptions.notFound("User not found"));
    }

    public Counterparty counterparty(UUID id) {
        if (id == null) {
            return null;
        }
        return requiredCounterparty(id);
    }

    public Counterparty requiredCounterparty(UUID id) {
        return counterpartyRepository.findById(id).orElseThrow(() -> Exceptions.notFound("Counterparty not found"));
    }
}
