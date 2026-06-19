package roomescape.infrastructure.payment;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.infrastructure.ratelimiter.TokenBucket;

@Configuration
public class TossClientConfig {

    @Bean
    public RestClient tossRestClient(
            @Value("${payment.toss.base-url:https://api.tosspayments.com}") String baseUrl,
            @Value("${payment.toss.secret-key}") String secretKey,
            @Value("${payment.toss.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${payment.toss.read-timeout-ms}") int readTimeoutMs,
            @Value("${payment.toss.max-attempts:3}") int maxAttempts,
            @Qualifier("outboundTokenBucket") TokenBucket outboundTokenBucket) {

        String basic = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .requestFactory(factory)
                .requestInterceptor(new TossOutboundRateLimitInterceptor(outboundTokenBucket))
                .requestInterceptor(new TossRetryInterceptor(maxAttempts))
                .build();
    }
}
