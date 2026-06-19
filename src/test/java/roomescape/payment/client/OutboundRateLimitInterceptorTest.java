package roomescape.payment.client;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import roomescape.infra.ratelimit.TokenBucket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboundRateLimitInterceptorTest {

    private static final byte[] EMPTY_BODY = new byte[0];

    @Test
    void 토큰이_있으면_요청을_그대로_통과시킨다() throws IOException {
        TokenBucket bucket = new TokenBucket(1, 1.0, () -> 0L);
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(bucket);

        var response = interceptor.intercept(request(), EMPTY_BODY, alwaysOk());

        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void 토큰이_없으면_OutboundRateLimitException을_던진다() {
        TokenBucket bucket = new TokenBucket(1, 1.0, () -> 0L);
        bucket.tryConsume(); // 토큰 소진
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(bucket);

        assertThatThrownBy(() ->
                interceptor.intercept(request(), EMPTY_BODY, alwaysOk())
        ).isInstanceOf(OutboundRateLimitException.class);
    }

    @Test
    void 토큰이_없으면_HTTP_요청을_전송하지_않는다() {
        TokenBucket bucket = new TokenBucket(1, 1.0, () -> 0L);
        bucket.tryConsume();
        AtomicInteger executionCount = new AtomicInteger(0);
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(bucket);

        assertThatThrownBy(() ->
                interceptor.intercept(request(), EMPTY_BODY, (req, body) -> {
                    executionCount.incrementAndGet();
                    return new MockClientHttpResponse(EMPTY_BODY, HttpStatus.OK);
                })
        ).isInstanceOf(OutboundRateLimitException.class);

        assertThat(executionCount.get()).isZero();
    }

    @Test
    void capacity만큼만_요청이_통과하고_이후_요청은_거부된다() throws IOException {
        int capacity = 3;
        TokenBucket bucket = new TokenBucket(capacity, 1.0, () -> 0L);
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(bucket);

        for (int i = 0; i < capacity; i++) {
            var response = interceptor.intercept(request(), EMPTY_BODY, alwaysOk());
            assertThat(response.getStatusCode().value()).isEqualTo(200);
        }

        assertThatThrownBy(() ->
                interceptor.intercept(request(), EMPTY_BODY, alwaysOk())
        ).isInstanceOf(OutboundRateLimitException.class);
    }

    @Test
    void 예외_상태코드는_429이다() {
        OutboundRateLimitException ex = new OutboundRateLimitException();

        assertThat(ex.getStatus().value()).isEqualTo(429);
        assertThat(ex.getCode()).isEqualTo("OUTBOUND_RATE_LIMITED");
    }

    // --- helpers ---

    private MockClientHttpRequest request() {
        return new MockClientHttpRequest(HttpMethod.POST,
                URI.create("https://api.tosspayments.com/v1/payments/confirm"));
    }

    private org.springframework.http.client.ClientHttpRequestExecution alwaysOk() {
        return (req, body) -> new MockClientHttpResponse(EMPTY_BODY, HttpStatus.OK);
    }
}