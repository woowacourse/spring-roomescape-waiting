package roomescape.payment.infra.client;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.payment.infra.client.interceptor.OutboundRateLimitInterceptor;
import roomescape.payment.infra.client.interceptor.RetryAfterInterceptor;
import roomescape.payment.presentation.ratelimit.policy.TokenBucketRateLimiter;

@Configuration
public class TossClientConfig {

    private final TokenBucketRateLimiter rateLimiter;

    public TossClientConfig(
            @Value("${outbound-rate-limit.capacity}") long outboundCapacity,
            @Value("${outbound-rate-limit.refill-per-second}") double outboundRefillPerSecond
    ) {
        this.rateLimiter = new TokenBucketRateLimiter(outboundCapacity, outboundRefillPerSecond, System::nanoTime);
    }

    @Bean
    public RestClient tossRestClient(
            @Value("${toss.base-url}") String baseUrl,
            @Value("${toss.secret-key}") String secretKey,
            @Value("${toss.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${toss.read-timeout-ms}") int readTimeoutMs,
            @Value("${toss.max-attempts}") int maxAttempts
    ) {
        String basic = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .requestInterceptor(new OutboundRateLimitInterceptor(rateLimiter))
                .requestInterceptor(new RetryAfterInterceptor(maxAttempts))
                .requestFactory(factory)
                .build();
    }
}
