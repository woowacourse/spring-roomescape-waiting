package roomescape.config;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.ratelimit.OutboundRateLimitInterceptor;
import roomescape.ratelimit.RetryAfterInterceptor;
import roomescape.ratelimit.TokenBucketRateLimiter;

@Configuration
public class TossClientConfig {

    @Bean
    public RestClient tossRestClient(
            @Value("${toss.base-url}") String baseUrl,
            @Value("${toss.secret-key}") String secretKey,
            @Value("${toss.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${toss.read-timeout-ms}") int readTimeoutMs,
            @Value("${toss.retry.max-attempts}") int maxAttempts,
            @Value("${toss.retry.fallback-seconds}") long fallbackSeconds,
            @Value("${outbound-rate-limit.capacity}") long outboundCapacity,
            @Value("${outbound-rate-limit.refill-per-sec}") double outboundRefillPerSec
    ) {
        String basic = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        // JDK 기본 팩토리는 응답 바디 지연을 read timeout으로 못 잡아 SimpleClientHttpRequestFactory 사용
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);

        TokenBucketRateLimiter outboundRateLimiter =
                new TokenBucketRateLimiter(outboundCapacity, outboundRefillPerSec, System::nanoTime);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .requestFactory(factory)
                .requestInterceptor(new RetryAfterInterceptor(maxAttempts, fallbackSeconds))
                .requestInterceptor(new OutboundRateLimitInterceptor(outboundRateLimiter))
                .build();
    }
}
