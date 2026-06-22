package roomescape.payment.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@EnableConfigurationProperties(PaymentProperties.class)
@Configuration
public class PaymentConfig {

    @Bean
    RestClient tossRestClient(RestClient.Builder builder, PaymentProperties properties) {
        return builder
                .baseUrl(properties.toss().baseUrl())
                .build();
    }
}
