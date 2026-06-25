package roomescape.payment.client;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class TossClientConfig {

    @Bean
    RestClient tossRestClient(
            @Value("${toss.base-url:https://api.tosspayments.com}") String baseUrl,
            @Value("${toss.secret-key:}") String secretKey,
            @Value("${toss.connect-timeout-ms:1000}") int connectTimeoutMs,
            @Value("${toss.read-timeout-ms:2000}") int readTimeoutMs
    ) {
        String encodedCredentials = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
                .requestFactory(requestFactory)
                .build();
    }
}
