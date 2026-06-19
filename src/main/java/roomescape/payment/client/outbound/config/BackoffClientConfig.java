package roomescape.payment.client.outbound.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import roomescape.payment.client.bucket.TokenBucketRateLimiter;
import roomescape.payment.client.outbound.interceptor.OutboundRateLimitInterceptor;
import roomescape.payment.client.outbound.interceptor.RetryAfterInterceptor;

@Configuration
public class BackoffClientConfig {

    @Bean
    public RestClient gatewayRestClient(
            @Value("${gateway.base-url}") String baseUrl,
            @Value("${gateway.max-attempts}") int maxAttempts,
            @Value("${outbound-rate-limit.capacity}") long outboundCapacity,
            @Value("${outbound-rate-limit.refill-per-second}") double outboundRefillPerSecond
    ) {
        var outboundLimiter = new TokenBucketRateLimiter(outboundCapacity, outboundRefillPerSecond, System::nanoTime);
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor(new OutboundRateLimitInterceptor(outboundLimiter))
                .requestInterceptor(new RetryAfterInterceptor(maxAttempts))
                .build();
    }

}
