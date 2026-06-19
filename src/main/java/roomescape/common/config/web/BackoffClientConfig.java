package roomescape.common.config.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import roomescape.infrastructure.ratelimit.TokenBucketRateLimiter;

/**
 * 게이트웨이 호출용 RestClient 빈 설정. 나가는 Rate Limit(바깥)과 Retry-After 백오프(안쪽)를 함께 건다.
 */
@Configuration
public class BackoffClientConfig {

    @Bean
    public RestClient gatewayRestClient(
            @Value("${gateway.base-url}") String baseUrl,
            @Value("${gateway.max-attempts}") int maxAttempts,
            @Value("${outbound-rate-limit.capacity}") long outboundCapacity,
            @Value("${outbound-rate-limit.refill-per-second}") double outboundRefillPerSecond
    ) {
        var outboundLimiter =
                new TokenBucketRateLimiter(outboundCapacity, outboundRefillPerSecond, System::nanoTime);
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor(new OutboundRateLimitInterceptor(outboundLimiter))
                .requestInterceptor(new RetryAfterInterceptor(maxAttempts))
                .build();
    }

}
