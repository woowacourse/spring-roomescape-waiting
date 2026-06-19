package roomescape.payment.client;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.payment.ratelimit.OutboundRateLimitInterceptor;
import roomescape.payment.ratelimit.RetryAfterInterceptor;
import roomescape.payment.ratelimit.TokenBucketRateLimiter;

/**
 * Toss 결제 API 호출용 RestClient 빈 설정. 인증은 Basic(시크릿키 + ":" 의 Base64)이다.
 * 인터셉터 순서: RetryAfter(외부) → OutboundRateLimit(내부) → 실제 HTTP
 */
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
            @Value("${outbound-rate-limit.refill-per-second}") double outboundRefillPerSecond
    ) {
        var basic = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);

        var outboundRateLimiter = new TokenBucketRateLimiter(outboundCapacity, outboundRefillPerSecond, System::nanoTime);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .requestFactory(factory)
                .requestInterceptor(new RetryAfterInterceptor(maxAttempts))
                .requestInterceptor(new OutboundRateLimitInterceptor(outboundRateLimiter))
                .build();
    }
}
