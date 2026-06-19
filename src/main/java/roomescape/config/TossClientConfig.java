package roomescape.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.function.LongSupplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.ratelimit.BackoffSleeper;
import roomescape.ratelimit.OutboundRateLimitInterceptor;
import roomescape.ratelimit.OutboundRateLimitProperties;
import roomescape.ratelimit.RetryAfterInterceptor;
import roomescape.ratelimit.TokenBucket;

@Configuration
public class TossClientConfig {

    @Bean
    public RestClient tossRestClient(
            @Value("${toss.base-url}") String baseUrl,
            @Value("${toss.secret-key}") String secretKey,
            @Value("${toss.connect-timeout}") Duration connectTimeout,
            @Value("${toss.read-timeout}") Duration readTimeout,
            OutboundRateLimitProperties rateLimitProperties,
            LongSupplier nanoTime,
            BackoffSleeper sleeper
    ) {
        String basic = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        OutboundRateLimitInterceptor outboundRateLimitInterceptor = new OutboundRateLimitInterceptor(rateLimitProperties,
                new TokenBucket(rateLimitProperties.capacity(), rateLimitProperties.refillPerSecond(), nanoTime));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory(connectTimeout, readTimeout))
                .requestInterceptor(outboundRateLimitInterceptor)
                .requestInterceptor(new RetryAfterInterceptor(rateLimitProperties.maxAttempts(), sleeper,
                        outboundRateLimitInterceptor::consumeToken))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .build();
    }

    private SimpleClientHttpRequestFactory requestFactory(Duration connectTimeout, Duration readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        return requestFactory;
    }
}
