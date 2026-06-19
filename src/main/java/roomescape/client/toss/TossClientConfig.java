package roomescape.client.toss;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.client.ratelimit.OutboundRateLimitInterceptor;
import roomescape.ratelimit.TokenBucketRateLimiter;

import static java.time.temporal.ChronoUnit.SECONDS;

@Configuration
public class TossClientConfig {

    private static final long DEFAULT_OUTBOUND_CAPACITY = 1_000_000L;
    private static final double DEFAULT_OUTBOUND_REFILL_PER_SEC = 1_000_000.0;
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final Duration DEFAULT_FALLBACK_DELAY = Duration.ofSeconds(1);

    @Bean
    public RestClient tossRestClient(
            @Value("${toss.base-url:https://api.tosspayments.com}") String baseUrl,
            @Value("${toss.secret-key:}") String secretKey,
            @Value("${toss.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${toss.read-timeout-ms}") int readTimeoutMs,
            @Value("${outbound-rate-limit.capacity:1000000}") long outboundCapacity,
            @Value("${outbound-rate-limit.refill-per-sec:1000000}") double outboundRefillPerSec,
            @Value("${outbound-rate-limit.retry.max-attempts:3}") int maxAttempts,
            @DurationUnit(SECONDS) @Value("${outbound-rate-limit.retry.fallback-delay:1s}") Duration fallbackDelay
    ) {
        String basic = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        TokenBucketRateLimiter outboundRateLimiter = new TokenBucketRateLimiter(
                outboundCapacity,
                outboundRefillPerSec,
                System::nanoTime
        );

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .requestFactory(factory)
                .requestInterceptor(new OutboundRateLimitInterceptor(outboundRateLimiter))
                .requestInterceptor(new RetryAfterInterceptor(maxAttempts, fallbackDelay))
                .build();
    }

    public RestClient tossRestClient(
            String baseUrl,
            String secretKey,
            int connectTimeoutMs,
            int readTimeoutMs
    ) {
        return tossRestClient(
                baseUrl,
                secretKey,
                connectTimeoutMs,
                readTimeoutMs,
                DEFAULT_OUTBOUND_CAPACITY,
                DEFAULT_OUTBOUND_REFILL_PER_SEC,
                DEFAULT_MAX_ATTEMPTS,
                DEFAULT_FALLBACK_DELAY
        );
    }
}
