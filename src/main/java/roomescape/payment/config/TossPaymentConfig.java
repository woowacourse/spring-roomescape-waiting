package roomescape.payment.config;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(TossPaymentProperties.class)
public class TossPaymentConfig {

    @Bean
    public RestClient tossRestClient(TossPaymentProperties properties) {
        String credentials = properties.secretKey() + ":";
        String basicToken = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basicToken)
                .build();
    }
}
