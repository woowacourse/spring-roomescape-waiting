package roomescape.payment.infrastructure.toss;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.common.ratelimit.TokenBucketRateLimiter;

@Configuration
public class TossClientConfig {

    @Bean("outboundRateLimiter")
    public TokenBucketRateLimiter outboundRateLimiter(
            @Value("${outbound-rate-limit.capacity}") long capacity,
            @Value("${outbound-rate-limit.refill-per-sec}") double refillPerSec
    ) {
        return new TokenBucketRateLimiter(capacity, refillPerSec, System::nanoTime);
    }

    @Bean("tossRestClient")
    public RestClient tossRestClient(
            @Value("${payment.toss.base-url}") String baseUrl,
            @Value("${payment.toss.secret-key}") String secretKey,
            @Value("${payment.toss.connection-request-timeout-ms}") int connectionRequestTimeoutMs,
            @Value("${payment.toss.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${payment.toss.read-timeout-ms}") int readTimeoutMs,
            @Value("${payment.toss.max-attempts}") int maxAttempts,
            TokenBucketRateLimiter outboundRateLimiter
    ) {
        String credential = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        var requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(connectionRequestTimeoutMs, TimeUnit.MILLISECONDS)
                .setConnectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
                .setResponseTimeout(readTimeoutMs, TimeUnit.MILLISECONDS)
                .build();

        var httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();

        var factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + credential)
                .requestInterceptor(new OutboundRateLimitInterceptor(outboundRateLimiter))
                .requestInterceptor(new RetryAfterInterceptor(maxAttempts))
                .requestFactory(factory)
                .build();
    }
}
