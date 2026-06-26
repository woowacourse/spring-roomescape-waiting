package roomescape.infrastructure.toss;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import roomescape.ratelimit.TokenBucketRateLimiter;

@Configuration
public class TossPaymentConfig {

    @Bean
    public ClientHttpRequestFactory tossClientHttpRequestFactory(
            @Value("${payment.toss.connect-timeout}") Duration connectTimeout,
            @Value("${payment.toss.read-timeout}") Duration readTimeout
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        return requestFactory;
    }

    @Bean
    public RestClient tossRestClient(
            RestClient.Builder restClientBuilder,
            ClientHttpRequestFactory tossClientHttpRequestFactory,
            RetryAfterInterceptor retryAfterInterceptor,
            OutboundRateLimitInterceptor outboundRateLimitInterceptor
    ) {
        return restClientBuilder
                .baseUrl("https://api.tosspayments.com")
                .requestFactory(tossClientHttpRequestFactory)
                .requestInterceptors(interceptors -> interceptors.addAll(List.of(
                        retryAfterInterceptor,
                        outboundRateLimitInterceptor
                )))
                .build();
    }

    @Bean
    public TokenBucketRateLimiter outboundRateLimiter(
            @Value("${outbound-rate-limit.capacity}") long capacity,
            @Value("${outbound-rate-limit.refill-per-sec}") double refillPerSecond
    ) {
        return new TokenBucketRateLimiter(capacity, refillPerSecond, System::nanoTime);
    }

    @Bean
    public OutboundRateLimitInterceptor outboundRateLimitInterceptor(
            @Qualifier("outboundRateLimiter") TokenBucketRateLimiter outboundRateLimiter
    ) {
        return new OutboundRateLimitInterceptor(outboundRateLimiter);
    }

    @Bean
    public RetryAfterInterceptor retryAfterInterceptor(
            @Value("${payment.toss.retry.max-attempts}") int maxAttempts,
            @Value("${payment.toss.retry.fallback-backoff}") Duration fallbackBackOff
    ) {
        return new RetryAfterInterceptor(maxAttempts, fallbackBackOff, duration -> Thread.sleep(duration.toMillis()));
    }
}
