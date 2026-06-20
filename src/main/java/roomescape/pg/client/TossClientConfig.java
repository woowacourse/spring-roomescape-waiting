package roomescape.pg.client;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.global.web.ratelimit.TokenBucketRateLimiter;

/**
 * Toss 결제 API 호출용 RestClient 빈 설정. 인증은 Basic(시크릿키 + ":" 의 Base64)이다.
 */
@Configuration
public class TossClientConfig {

  @Bean
  public RestClient tossRestClient(
      @Value("${toss.base-url}") String baseUrl,
      @Value("${toss.secret-key}") String secretKey,
      @Value("${toss.connect-timeout-ms}") long connectTimeoutMs,
      @Value("${toss.read-timeout-ms}") long readTimeoutMs,
      @Value("${toss.max-attempts}") int maxAttempts,
      @Value("${outbound-rate-limit.capacity}") long outboundCapacity,
      @Value("${outbound-rate-limit.refill-per-second}") double outboundRefillPerSecond
  ) {
    var basic = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
    var outboundLimiter = new TokenBucketRateLimiter(outboundCapacity, outboundRefillPerSecond, System::nanoTime);

    return RestClient.builder()
        .baseUrl(baseUrl)
        .requestFactory(createRequestFactory(connectTimeoutMs, readTimeoutMs))
        .requestInterceptor(new OutboundRateLimitInterceptor(outboundLimiter))
        .requestInterceptor(new RetryAfterInterceptor(maxAttempts))
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
        .build();
  }

  private SimpleClientHttpRequestFactory createRequestFactory(long connectTimeoutMs, long readTimeoutMs) {
    var requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
    requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
    return requestFactory;
  }

}
