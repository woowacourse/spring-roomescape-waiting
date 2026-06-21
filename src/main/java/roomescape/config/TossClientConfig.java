package roomescape.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.payment.RetryAfterInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class TossClientConfig {

    @Bean
    public RestClient tossRestClient(
            @Value("${toss.base-url}") String baseUrl,
            @Value("${toss.secret-key}") String secretKey,
            @Value("${toss.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${toss.read-timeout-ms}") int readTimeoutMs,
            @Value("${toss.max-attempts:3}") int maxAttempts
    ) {
        final String basic = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .requestFactory(factory)
                .requestInterceptor(new RetryAfterInterceptor(maxAttempts))
                .build();
    }
}
