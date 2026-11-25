package np.org.nrb.pars.nrb.config;

import np.org.nrb.pars.nrb.client.NrbForexApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/** Single configured outbound HTTP client for official NRB sources. */
@Configuration
public class NrbHttpConfiguration {

    @Bean
    public RestClient nrbRestClient(NrbProperties properties) {
        // Redirects are NOT followed automatically: every hop is re-validated by
        // NrbUrlPolicy in the crawler/downloader. This prevents allow-list bypass.
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(properties.getResponseTimeout());

        return RestClient.builder()
                .baseUrl(properties.getForexApiBaseUrl())
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.USER_AGENT, properties.getCrawlerUserAgent())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public NrbForexApiClient nrbForexApiClient(RestClient nrbRestClient, NrbProperties properties) {
        return new NrbForexApiClient(nrbRestClient, properties);
    }
}
