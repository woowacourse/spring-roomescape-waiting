package roomescape.payment.toss;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import roomescape.common.ratelimit.TokenBucketRateLimiter;

/**
 * 토스 연동 설정. 토스용 RestClient를 여기서 조립한다 — base-url과 서버 승인용 Basic 인증 헤더를
 * 기본값으로 박아, 어댑터(TossPaymentGateway)는 '어느 경로에 무엇을 보낼지'만 알면 되게 한다.
 * 나가는 호출엔 인터셉터 두 겹을 건다 — 바깥은 한도 검사(OutboundRateLimitInterceptor),
 * 안쪽은 429 백오프 재시도(RetryAfterInterceptor)다.
 */
@Configuration
@EnableConfigurationProperties({TossProperties.class, OutboundRateLimitProperties.class})
public class TossConfig {

    @Bean
    public OutboundRateLimitInterceptor outboundRateLimitInterceptor(OutboundRateLimitProperties properties) {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(
                properties.capacity(), properties.refillPerSec(), System::nanoTime);
        return new OutboundRateLimitInterceptor(rateLimiter);
    }

    @Bean
    public RetryAfterInterceptor retryAfterInterceptor(
            @Value("${toss.retry.max-attempts:3}") int maxAttempts,
            @Value("${toss.retry.default-backoff-ms:1000}") long defaultBackoffMillis) {
        return new RetryAfterInterceptor(maxAttempts, defaultBackoffMillis, BackoffSleeper.realTime());
    }

    @Bean
    public RestClient tossRestClient(RestClient.Builder builder, TossProperties properties,
                                     OutboundRateLimitInterceptor outboundRateLimitInterceptor,
                                     RetryAfterInterceptor retryAfterInterceptor) {
        // Basic 인증: base64(시크릿키 + ":"). 콜론 뒤 비밀번호는 비우고 UTF-8로 인코딩한다.
        String basic = Base64.getEncoder()
                .encodeToString((properties.secretKey() + ":").getBytes(StandardCharsets.UTF_8));
        // 타임아웃은 연결을 실제로 만드는 요청 팩토리에 건다(고수준 builder엔 해당 설정이 없다).
        // Apache HttpComponents를 쓴다: simple(HttpURLConnection)은 401 응답 바디를 인증 처리 중
        // 삼켜 토스 에러코드 매핑이 깨지고, jdk는 응답 바디 지연을 read timeout으로 못 잡는다.
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(properties.connectTimeout())
                .withReadTimeout(properties.readTimeout());
        // Apache HttpClient5의 기본 재시도(429·503 자동 재시도)를 끈다 — 429 백오프 재시도는
        // RetryAfterInterceptor가 단독으로 책임지게 해, 이중 재시도/우리 maxAttempts 무시를 막는다.
        ClientHttpRequestFactory factory = ClientHttpRequestFactoryBuilder
                .httpComponents()
                .withHttpClientCustomizer(HttpClientBuilder::disableAutomaticRetries)
                .build(settings);
        return builder
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                // 순서: 바깥(먼저 등록)부터 적용된다. 한도 검사 → (통과 시) 실제 전송 + 429 백오프 재시도.
                .requestInterceptor(outboundRateLimitInterceptor)
                .requestInterceptor(retryAfterInterceptor)
                .requestFactory(factory)
                .build();
    }
}
