package roomescape.adapter.payment;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.ratelimit.OutboundRateLimitProperties;
import roomescape.ratelimit.TokenBucketRateLimiter;

/**
 * Toss 결제 API 호출용 RestClient 빈. 인증은 Basic(시크릿키 + ":" 의 Base64)이다. (타임아웃 설정은 mission step2에서 추가한다.)
 */
@Configuration
@EnableConfigurationProperties({TossProperties.class, OutboundRateLimitProperties.class})
public class TossClientConfig {

    @Bean
    public RestClient tossRestClient(TossProperties properties, OutboundRateLimitProperties outboundProperties) {
        String basic = Base64.getEncoder()
                .encodeToString((properties.secretKey() + ":").getBytes(StandardCharsets.UTF_8));

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.connectTimeoutMs());
        factory.setReadTimeout(properties.readTimeoutMs());

        TokenBucketRateLimiter outboundLimiter = new TokenBucketRateLimiter(
                outboundProperties.capacity(), outboundProperties.refillPerSecond(), System::nanoTime);

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .requestFactory(factory)
                .requestInterceptor(new RetryAfterInterceptor(properties.maxAttempts()))
                .requestInterceptor(new OutboundRateLimitInterceptor(outboundLimiter))
                .build();
    }
}
