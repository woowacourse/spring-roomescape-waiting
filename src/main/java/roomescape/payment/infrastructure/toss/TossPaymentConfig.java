package roomescape.payment.infrastructure.toss;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

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
    public RestClient tossRestClient(
            final TossPaymentProperties properties,
            final TossRateLimitRetryInterceptor rateLimitRetryInterceptor
    ) {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) properties.connectTimeout().toMillis());
        requestFactory.setReadTimeout((int) properties.readTimeout().toMillis());

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .requestInterceptor(rateLimitRetryInterceptor)
                .build();
    }
}
