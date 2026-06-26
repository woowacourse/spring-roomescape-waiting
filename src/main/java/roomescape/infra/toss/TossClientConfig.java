package roomescape.infra.toss;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import roomescape.ratelimit.TokenBucketRateLimiter;

@Configuration
public class TossClientConfig {

    @Bean
    public RestClient tossRestClient(
            @Value("${toss.base-url}") String baseUrl,
            @Value("${toss.secret-key}") String secretKey,
            @Value("${toss.max-attempts}") int maxAttempts,
            @Value("${toss.fallback-retry-after-seconds}") long fallbackRetryAfterSeconds,
            @Value("${outbound-rate-limit.capacity}") long outboundCapacity,
            @Value("${outbound-rate-limit.refill-per-second}") double outboundRefillPerSecond
    ) {
        String basic = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        TokenBucketRateLimiter outboundRateLimiter = new TokenBucketRateLimiter(
                outboundCapacity,
                outboundRefillPerSecond,
                System::nanoTime
        );

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .requestInterceptor(new OutboundRateLimitInterceptor(outboundRateLimiter))
                .requestInterceptor(new RetryAfterInterceptor(maxAttempts, fallbackRetryAfterSeconds))
                .build();
    }
}
