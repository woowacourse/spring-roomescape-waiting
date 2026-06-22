package roomescape.payment.toss;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class TossClientConfig {

    @Bean
    public RestClient tossRestClient(
            @Value("${toss.base-url}") String baseUrl,
            @Value("${toss.secret-key}") String secretKey,
            @Value("${toss.client.connect-timeout}") Duration connectTimeout,
            @Value("${toss.client.read-timeout}") Duration readTimeout,
            @Value("${toss.client.max-attempts}") int maxAttempts,
            @Value("${toss.client.retry-after-fallback}") Duration retryAfterFallback
    ) {
        String credential = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + credential)
                .requestInterceptor(new RetryAfterInterceptor(maxAttempts, retryAfterFallback))
                .requestFactory(requestFactory)
                .build();
    }
}
