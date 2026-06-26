package roomescape.infra.toss;

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
            @Value("${toss.max-attempts}") int maxAttempts,
            @Value("${toss.fallback-retry-after-seconds}") long fallbackRetryAfterSeconds,
            @Value("${outbound-rate-limit.capacity}") long outboundCapacity,
            @Value("${outbound-rate-limit.refill-per-second}") double outboundRefillPerSecond,
            @Value("${toss.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${toss.read-timeout-ms}") int readTimeoutMs
    ) {
        String basic = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        TokenBucketRateLimiter outboundRateLimiter = new TokenBucketRateLimiter(
                outboundCapacity,
                outboundRefillPerSecond,
                System::nanoTime
        );

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .requestFactory(factory)
                .requestInterceptor(new OutboundRateLimitInterceptor(outboundRateLimiter))
                .requestInterceptor(new RetryAfterInterceptor(maxAttempts, fallbackRetryAfterSeconds))
                .build();
    }
}
