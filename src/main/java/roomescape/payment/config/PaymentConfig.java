package roomescape.payment.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@EnableConfigurationProperties(PaymentProperties.class)
@Configuration
public class PaymentConfig {

    @Bean
    RestClient tossRestClient(RestClient.Builder builder, PaymentProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.toss().connectTimeout());
        requestFactory.setReadTimeout(properties.toss().readTimeout());

        return builder
                .baseUrl(properties.toss().baseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}
