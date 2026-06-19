package roomescape.infrastructure.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.infrastructure.ratelimiter.TokenBucket;

class TossOutboundRateLimitInterceptorTest {

    @Test
    @DisplayName("토큰이 없으면 Toss로 요청을 보내기 전에 차단한다.")
    void rejectBeforeCallingTossWhenTokenIsEmpty() {
        AtomicLong now = new AtomicLong(0);
        TokenBucket tokenBucket = new TokenBucket(1, 0.1, now::get);
        tokenBucket.tryConsume();

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.baseUrl("https://api.tosspayments.com")
                .requestInterceptor(new TossOutboundRateLimitInterceptor(tokenBucket))
                .build();

        assertThatThrownBy(() -> restClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .retrieve()
                .toBodilessEntity())
                .isInstanceOf(OutboundRateLimitException.class)
                .extracting("retryAfterSeconds")
                .isEqualTo(10L);

        server.verify();
    }

    @Test
    @DisplayName("토큰이 있으면 Toss로 요청을 보낸다.")
    void callTossWhenTokenExists() {
        TokenBucket tokenBucket = new TokenBucket(1, 0.1, () -> 0L);

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.baseUrl("https://api.tosspayments.com")
                .requestInterceptor(new TossOutboundRateLimitInterceptor(tokenBucket))
                .build();

        server.expect(once(), requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andRespond(withSuccess());

        restClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .retrieve()
                .toBodilessEntity();

        server.verify();
    }

    @Test
    @DisplayName("토큰이 보충되면 다시 외부 요청을 보낼 수 있다.")
    void refillAllowsNextCall() {
        AtomicLong now = new AtomicLong(0);
        TokenBucket tokenBucket = new TokenBucket(1, 1, now::get);

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.baseUrl("https://api.tosspayments.com")
                .requestInterceptor(new TossOutboundRateLimitInterceptor(tokenBucket))
                .build();

        server.expect(once(), requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andRespond(withSuccess());
        server.expect(once(), requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andRespond(withSuccess());

        restClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .retrieve()
                .toBodilessEntity();

        assertThat(tokenBucket.tryConsume()).isFalse();
        now.addAndGet(1_000_000_000L);

        restClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .retrieve()
                .toBodilessEntity();

        server.verify();
    }
}
