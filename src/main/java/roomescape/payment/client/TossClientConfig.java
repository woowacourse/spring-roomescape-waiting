package roomescape.payment.client;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.infra.ratelimit.TokenBucket;

@Configuration
@EnableConfigurationProperties({TossRetryProperties.class, OutboundRateLimitProperties.class})
public class TossClientConfig {

    @Bean
    public RestClient tossRestClient(
            @Value("${toss.base-url}") String baseUrl,
            @Value("${toss.secret-key}") String secretKey,
            @Value("${toss.connect-timeout}") Duration connectTimeout,
            @Value("${toss.read-timeout}") Duration readTimeout,
            TossRetryProperties retryProperties,
            OutboundRateLimitProperties outboundProperties
    ) {
        String basic = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        TokenBucket outboundBucket = TokenBucket.ofRealTime(
                outboundProperties.capacity(), outboundProperties.refillPerSec()
        );

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .requestFactory(factory)
                .requestInterceptor(new OutboundRateLimitInterceptor(outboundBucket))
                .requestInterceptor(new TossRetryInterceptor(
                        retryProperties.maxAttempts(),
                        retryProperties.fallbackWait(),
                        Sleeper.threadSleep()
                ))
                .build();
    }
}