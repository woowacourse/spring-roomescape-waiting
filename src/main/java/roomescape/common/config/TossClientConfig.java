package roomescape.common.config;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.common.ratelimit.TokenBucketRateLimiter;
import roomescape.common.ratelimit.interceptor.OutboundRateLimitInterceptor;
import roomescape.common.ratelimit.interceptor.RetryAfterInterceptor;

@Configuration
public class TossClientConfig {

    @Bean
    public RestClient tossRestClient(
            @Value("${toss.base-url}") String baseUrl,
            @Value("${toss.secret-key}") String secretKey,
            @Value("${toss.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${toss.read-timeout-ms}") int readTimeoutMs,
            @Value("${gateway.max-attempts}") int maxAttempts,
            @Value("${outbound-rate-limit.capacity}") long outboundCapacity,
            @Value("${outbound-rate-limit.refill-per-second}") double outboundRefillPerSecond
    ) {
        String basic = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);

        TokenBucketRateLimiter outboundLimiter =
                new TokenBucketRateLimiter(outboundCapacity, outboundRefillPerSecond, System::nanoTime);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .requestFactory(requestFactory)
                .requestInterceptor(new OutboundRateLimitInterceptor(outboundLimiter))
                .requestInterceptor(new RetryAfterInterceptor(maxAttempts))
                .build();
    }
}
