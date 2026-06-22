package roomescape.payment.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.payment.infra.toss.OutboundRateLimitInterceptor;
import roomescape.payment.infra.toss.RetryAfterInterceptor;
import roomescape.ratelimit.TokenBucketRateLimiter;

@EnableConfigurationProperties({PaymentProperties.class, TossRetryProperties.class})
@Configuration
public class PaymentConfig {

    @Bean
    RestClient tossRestClient(
            RestClient.Builder builder,
            PaymentProperties properties,
            TossRetryProperties retryProperties,
            @Qualifier("outboundRateLimiter") TokenBucketRateLimiter outboundRateLimiter
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.toss().connectTimeout());
        requestFactory.setReadTimeout(properties.toss().readTimeout());
        OutboundRateLimitInterceptor outboundInterceptor =
                new OutboundRateLimitInterceptor(outboundRateLimiter);

        return builder
                .baseUrl(properties.toss().baseUrl())
                .requestFactory(requestFactory)
                .requestInterceptor(outboundInterceptor)
                .requestInterceptor(new RetryAfterInterceptor(
                        retryProperties.maxAttempts(),
                        retryProperties.fallbackDelay(),
                        outboundInterceptor::acquire
                ))
                .build();
    }
}
