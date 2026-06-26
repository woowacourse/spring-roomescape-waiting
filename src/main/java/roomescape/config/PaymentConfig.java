package roomescape.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.payment.adapter.TossPaymentGateway;
import roomescape.payment.port.PaymentGateway;
import roomescape.ratelimit.OutboundRateLimitInterceptor;
import roomescape.ratelimit.OutboundRateLimitProperties;
import roomescape.ratelimit.RetryAfterInterceptor;
import roomescape.ratelimit.TokenBucketRateLimiter;

@Configuration
@EnableConfigurationProperties(TossPaymentProperties.class)
public class PaymentConfig {

    @Bean
    public TokenBucketRateLimiter outboundRateLimiter(OutboundRateLimitProperties properties) {
        return new TokenBucketRateLimiter(properties.capacity(), properties.refillPerSec(), System::nanoTime);
    }

    @Bean
    public RestClient tossRestClient(TossPaymentProperties properties, TokenBucketRateLimiter outboundRateLimiter) {
        String encoded = Base64.getEncoder()
                .encodeToString((properties.secretKey() + ":").getBytes(StandardCharsets.UTF_8));

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.connectTimeoutMs());
        factory.setReadTimeout(properties.readTimeoutMs());

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(factory)
                .requestInterceptor(new RetryAfterInterceptor(properties.maxRetryAttempts()))
                .requestInterceptor(new OutboundRateLimitInterceptor(outboundRateLimiter))
                .build();
    }

    @Bean
    public PaymentGateway paymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        return new TossPaymentGateway(tossRestClient, objectMapper);
    }
}
