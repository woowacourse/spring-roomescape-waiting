package roomescape.payment.toss;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import roomescape.ratelimit.OutboundRateLimitInterceptor;
import roomescape.ratelimit.RetryAfterInterceptor;

@Configuration
public class TossRestClientConfig {

    @Bean
    public RestClient tossRestClient(
            TossPaymentProperties properties,
            RetryAfterInterceptor retryAfterInterceptor,
            OutboundRateLimitInterceptor outboundRateLimitInterceptor
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());

        return RestClient.builder()
                .requestFactory(requestFactory)
                .requestInterceptor(retryAfterInterceptor)
                .requestInterceptor(outboundRateLimitInterceptor)
                .build();
    }
}
