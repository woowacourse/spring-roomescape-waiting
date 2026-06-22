package roomescape.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestClient;
import roomescape.infra.toss.OutboundRateLimitInterceptor;
import roomescape.infra.toss.RetryAfterInterceptor;

import java.util.Base64;

@EnableRetry
@Configuration
public class TossPaymentConfig {

    @Value("${toss.secret-key}")
    private String secretKey;

    @Value("${toss.connect-timeout}")
    private int connectTimeout;

    @Value("${toss.read-timeout}")
    private int readTimeout;

    @Bean
    public RestClient tossRestClient(OutboundRateLimitInterceptor outboundRateLimitInterceptor,
                                     RetryAfterInterceptor retryAfterInterceptor) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        String encoded = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
        return RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://api.tosspayments.com")
                .defaultHeader("Authorization", "Basic " + encoded)
                .defaultHeader("Content-Type", "application/json")
                .requestInterceptors(interceptors -> {
                    interceptors.add(outboundRateLimitInterceptor);
                    interceptors.add(retryAfterInterceptor);
                })
                .build();
    }
}
