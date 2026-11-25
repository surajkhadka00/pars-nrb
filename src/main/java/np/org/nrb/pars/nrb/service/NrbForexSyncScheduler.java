package np.org.nrb.pars.nrb.service;

import np.org.nrb.pars.nrb.config.NrbProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/** Keeps a bounded recent forex history durable. Manual sync endpoint also available. */
@Component
public class NrbForexSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(NrbForexSyncScheduler.class);
    private final NrbForexService forexService;
    private final NrbProperties properties;

    public NrbForexSyncScheduler(NrbForexService forexService, NrbProperties properties) {
        this.forexService = forexService;
        this.properties = properties;
    }

    @Scheduled(cron = "${nrb.sync-cron:0 0 6 * * *}", zone = "Asia/Kathmandu")
    public void syncRecentForex() {
        try {
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Kathmandu"));
            int saved = forexService.sync(today.minusDays(properties.getLookbackDays()), today);
            log.info("PARS NRB forex sync saved {} new day(s)", saved);
        } catch (Exception e) {
            log.warn("PARS NRB forex sync failed: {}", e.getMessage());
        }
    }
}
