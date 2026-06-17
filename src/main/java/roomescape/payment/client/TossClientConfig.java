package roomescape.payment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import java.util.Base64;

@Configuration
public class TossClientConfig {

    @Value("${toss.base-url}")
    private String baseUrl;

    @Value("${toss.secret-key}")
    private String secretKey;

    @Bean
    public RestClient tossRestClient() {
        String encoded = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
