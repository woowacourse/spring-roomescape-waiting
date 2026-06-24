package roomescape.infrastructure.toss;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

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
            ClientHttpRequestFactory tossClientHttpRequestFactory
    ) {
        return restClientBuilder
                .baseUrl("https://api.tosspayments.com")
                .requestFactory(tossClientHttpRequestFactory)
                .build();
    }
}
