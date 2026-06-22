package roomescape.client;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.ratelimit.TokenBucketRateLimiter;

/**
 * Toss 결제 API 호출용 RestClient 빈 설정. 인증은 Basic(시크릿키 + ":" 의 Base64)이다.
 * 나가는 호출 한 곳에 Rate Limit(바깥)과 Retry-After 백오프(안쪽)를 함께 건다 — 한도는 보내기 전에
 * 스스로 조절하고, 그래도 받은 429 는 백오프 재시도한다.
 */
@Configuration
public class TossClientConfig {

    @Bean
    public RestClient tossRestClient(
            @Value("${toss.base-url}") String baseUrl,
            @Value("${toss.secret-key}") String secretKey,
            @Value("${toss.connect-timeout-ms}") Long timeoutMs,
            @Value("${toss.read-timeout-ms}") Long readTimeoutMs,
            @Value("${toss.max-attempts}") int maxAttempts,
            @Value("${outbound-rate-limit.capacity}") long outboundCapacity,
            @Value("${outbound-rate-limit.refill-per-second}") double outboundRefillPerSecond
    ) {
        String basic = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        TokenBucketRateLimiter outboundLimiter =
                new TokenBucketRateLimiter(outboundCapacity, outboundRefillPerSecond, System::nanoTime);
        return RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                // 바깥: 나가는 호출 한도를 넘으면 보내기 전에 스스로 거부한다(재시도마다 토큰을 또 쓰지 않게 가장 바깥).
                .requestInterceptor(new OutboundRateLimitInterceptor(outboundLimiter))
                // 안쪽: 그래도 토스가 429 를 주면 Retry-After 만큼 대기 후 재시도(백오프)한다.
                .requestInterceptor(new RetryAfterInterceptor(maxAttempts))
                .build();
    }
}
