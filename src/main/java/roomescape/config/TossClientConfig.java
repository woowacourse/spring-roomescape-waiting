package roomescape.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.ratelimit.OutboundRateLimitInterceptor;
import roomescape.ratelimit.RetryAfterInterceptor;
import roomescape.ratelimit.TokenBucketRateLimiter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(OutboundRateLimitProperties.class)
public class TossClientConfig {

    @Bean
    public RestClient tossRestClient(
            @Value("${toss.secret-key}") String secretKey,
            @Value("${toss.base-url}") String baseUrl,
            @Value("${toss.connect-timeout}") int connectTimeout,
            @Value("${toss.read-timeout}") int readTimeout,
            @Value("${toss.retry-max-attempts:3}") int retryMaxAttempts,
            OutboundRateLimitProperties outboundProps) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        TokenBucketRateLimiter outboundLimiter = new TokenBucketRateLimiter(
                outboundProps.getCapacity(),
                outboundProps.getRefillPerSec(),
                System::nanoTime
        );

        String encoded = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
                .requestInterceptor(new OutboundRateLimitInterceptor(outboundLimiter))
                .requestInterceptor(new RetryAfterInterceptor(retryMaxAttempts))
                .build();
    }
}