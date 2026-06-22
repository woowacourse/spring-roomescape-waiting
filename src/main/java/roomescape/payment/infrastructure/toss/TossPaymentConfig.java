package roomescape.payment.infrastructure.toss;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.common.ratelimit.TokenBucketRateLimiter;

@Configuration
@EnableConfigurationProperties(TossPaymentProperties.class)
public class TossPaymentConfig {

    @Bean
    public TossRateLimitRetryInterceptor tossRateLimitRetryInterceptor(final TossPaymentProperties properties) {
        return new TossRateLimitRetryInterceptor(
                properties.rateLimitRetry().maxAttempts(),
                properties.rateLimitRetry().fallbackBackoff(),
                new ThreadBackoffSleeper()
        );
    }

    @Bean
    public TossOutboundRateLimitInterceptor tossOutboundRateLimitInterceptor(
            @Qualifier("outboundRateLimiter") final TokenBucketRateLimiter rateLimiter
    ) {
        return new TossOutboundRateLimitInterceptor(rateLimiter);
    }

    @Bean
    public RestClient tossRestClient(
            final TossPaymentProperties properties,
            final TossRateLimitRetryInterceptor rateLimitRetryInterceptor,
            final TossOutboundRateLimitInterceptor outboundRateLimitInterceptor
    ) {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) properties.connectTimeout().toMillis());
        requestFactory.setReadTimeout((int) properties.readTimeout().toMillis());

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .requestInterceptor(rateLimitRetryInterceptor)
                .requestInterceptor(outboundRateLimitInterceptor)
                .build();
    }
}
