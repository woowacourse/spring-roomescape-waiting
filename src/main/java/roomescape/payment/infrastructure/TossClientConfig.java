package roomescape.payment.infrastructure;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.global.ratelimit.TokenBucketRateLimiter;

@Configuration
@EnableConfigurationProperties(TossProperties.class)
public class TossClientConfig {

    @Bean
    public RestClient tossRestClient(
            TossProperties properties,
            @Value("${outbound-rate-limit.capacity:5}") long outboundCapacity,
            @Value("${outbound-rate-limit.refill-per-second:2}") double outboundRefillPerSec
    ) {
        String encodedCredentials = Base64.getEncoder()
                .encodeToString((properties.secretKey() + ":").getBytes(StandardCharsets.UTF_8));

        // jdk 팩토리는 응답 바디 지연을 read timeout 으로 못 잡아(JDK-8258397) simple 을 쓴다.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.connectTimeoutMs());
        factory.setReadTimeout(properties.readTimeoutMs());

        TokenBucketRateLimiter outboundRateLimiter =
                new TokenBucketRateLimiter(outboundCapacity, outboundRefillPerSec, System::nanoTime);

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(factory)
                .requestInterceptors(interceptors -> {
                    // 나가는 호출은 자체 한도 게이트(바깥)를 먼저 통과해야 토스로 보내고,
                    // 토스가 429를 주면 안쪽에서 Retry-After 만큼 대기 후 재시도한다.
                    interceptors.add(new OutboundRateLimitInterceptor(outboundRateLimiter));
                    interceptors.add(new RetryAfterInterceptor(properties.maxAttempts()));
                })
                .build();
    }
}
