package roomescape.payment.toss;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.payment.ratelimit.TokenBucketRateLimiter;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Configuration
public class TossClientConfig {

    @Bean
    public TokenBucketRateLimiter outboundRateLimiter(
            @Value("${outbound-rate-limit.capacity}") long capacity,
            @Value("${outbound-rate-limit.refill-per-second}") double refillPerSecond
    ) {
        return new TokenBucketRateLimiter(capacity, refillPerSecond, System::nanoTime);
    }

    @Bean
    public RestClient tossRestClient(
            @Value("${toss.base-url}") String baseUrl,
            @Value("${toss.secret-key}") String secretKey,
            @Value("${toss.connect-timeout}") Duration connectTimeout,
            @Value("${toss.read-timeout}") Duration readTimeout,
            @Value("${toss.max-attempts}") int maxAttempts,
            TokenBucketRateLimiter outboundRateLimiter
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        String basic = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        return RestClient.builder()
                .requestFactory(factory)
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .requestInterceptor(new OutboundRateLimitInterceptor(outboundRateLimiter))
                .requestInterceptor(new RetryAfterInterceptor(maxAttempts))
                .build();
    }
}
