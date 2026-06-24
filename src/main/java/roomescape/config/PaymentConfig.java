package roomescape.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import roomescape.payment.adapter.TossPaymentGateway;
import roomescape.payment.port.PaymentGateway;

@Configuration
@EnableConfigurationProperties(TossPaymentProperties.class)
public class PaymentConfig {

    @Bean
    public RestClient tossRestClient(TossPaymentProperties properties) {
        String encoded = Base64.getEncoder()
                .encodeToString((properties.secretKey() + ":").getBytes(StandardCharsets.UTF_8));

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public PaymentGateway paymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        return new TossPaymentGateway(tossRestClient, objectMapper);
    }
}
