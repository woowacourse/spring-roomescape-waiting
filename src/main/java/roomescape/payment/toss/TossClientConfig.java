package roomescape.payment.toss;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.ratelimit.TokenBucketRateLimiter;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Configuration
public class TossClientConfig {

    @Bean
    public Sleeper tossBackoffSleeper() {
        return new ThreadSleeper();
    }

    @Bean
    public RetryAfterInterceptor tossRetryAfterInterceptor(
            @Value("${toss.retry.max-attempts}") int maxAttempts,
            @Value("${toss.retry.default-backoff}") Duration defaultBackoff,
            Sleeper tossBackoffSleeper
    ) {
        return new RetryAfterInterceptor(maxAttempts, defaultBackoff, tossBackoffSleeper);
    }

    @Bean
    public OutboundRateLimitInterceptor tossOutboundRateLimitInterceptor(
            @Qualifier("outboundRateLimiter") TokenBucketRateLimiter outboundRateLimiter
    ) {
        return new OutboundRateLimitInterceptor(outboundRateLimiter);
    }

    @Bean
    public RestClient tossRestClient(
            @Value("${toss.base-url}") String baseUrl,
            @Value("${toss.secret-key}") String secretKey,
            @Value("${toss.connect-timeout}") Duration connectTimeout,
            @Value("${toss.read-timeout}") Duration readTimeout,
            RetryAfterInterceptor tossRetryAfterInterceptor,
            OutboundRateLimitInterceptor tossOutboundRateLimitInterceptor
    ) {
        String basic = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .requestInterceptor(tossRetryAfterInterceptor)
                .requestInterceptor(tossOutboundRateLimitInterceptor)
                .build();
    }
}
