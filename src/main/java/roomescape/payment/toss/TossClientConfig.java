package roomescape.payment.toss;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class TossClientConfig {

    @Bean("tossRestClient")
    public RestClient tossRestClient(
            @Value("${payment.toss.base-url}") String baseUrl,
            @Value("${payment.toss.secret-key:}") String secretKey
    ) {
        String credential = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + credential)
                .build();
    }
}
