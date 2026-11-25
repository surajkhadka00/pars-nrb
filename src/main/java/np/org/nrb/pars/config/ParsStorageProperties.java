package np.org.nrb.pars.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pars.storage")
public class ParsStorageProperties {

    /**
     * Directory for deal attachment files. Set per profile in application-dev.yml,
     * application-stag.yml, and application-prod.yml.
     */
    private String dealAttachments;

    public String getDealAttachments() {
        return dealAttachments;
    }

    public void setDealAttachments(String dealAttachments) {
        this.dealAttachments = dealAttachments;
    }
}
