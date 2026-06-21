package roomescape.infrastructure.payment.client;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.ratelimit.TokenBucketRateLimiter;

@Configuration
public class TossClientConfig {

    @Bean
    public RestClient tossRestClient(
            @Value("${toss.base-url}") String baseUrl,
            @Value("${toss.secret-key}") String secretKey,
            @Value("${toss.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${toss.read-timeout-ms}") int readTimeoutMs,
            @Value("${toss.retry.max-attempts:3}") int maxAttempts,
            @Value("${outbound-rate-limit.capacity}") long outboundCapacity,
            @Value("${outbound-rate-limit.refill-per-sec}") double outboundRefillPerSec
    ) {
        TokenBucketRateLimiter outboundRateLimiter =
                new TokenBucketRateLimiter(outboundCapacity, outboundRefillPerSec, System::nanoTime);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodeBasicAuth(secretKey))
                .requestFactory(buildRequestFactory(connectTimeoutMs, readTimeoutMs))
                .requestInterceptor(new OutboundRateLimitClientInterceptor(outboundRateLimiter))
                .requestInterceptor(new TossRetryInterceptor(maxAttempts))
                .build();
    }

    private String encodeBasicAuth(String secretKey) {
        return Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
    }

    private SimpleClientHttpRequestFactory buildRequestFactory(int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return factory;
    }
}
