package roomescape.payment.infra.toss;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import roomescape.global.ratelimit.TokenBucketRateLimiter;
import roomescape.payment.domain.exception.OutboundRateLimitException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboundRateLimitInterceptorTest {

    @Test
    @DisplayName("자체 한도를 넘으면 외부로 보내지 않고 즉시 거부한다.")
    void intercept_rejectsBeforeExecutionWhenLimited() throws Exception {
        AtomicLong clock = new AtomicLong(0);
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(
                new TokenBucketRateLimiter(2, 1.0, clock::get)
        );
        AtomicInteger executions = new AtomicInteger();

        interceptor.intercept(request(), new byte[0], (request, body) -> okResponse(executions));
        interceptor.intercept(request(), new byte[0], (request, body) -> okResponse(executions));

        assertThatThrownBy(() -> interceptor.intercept(request(), new byte[0], (request, body) -> okResponse(executions)))
                .isInstanceOf(OutboundRateLimitException.class);
        assertThat(executions.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("토큰이 보충되면 다시 외부로 나간다.")
    void intercept_allowsAgainAfterRefill() throws Exception {
        AtomicLong clock = new AtomicLong(0);
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1.0, clock::get)
        );
        AtomicInteger executions = new AtomicInteger();

        interceptor.intercept(request(), new byte[0], (request, body) -> okResponse(executions));
        assertThatThrownBy(() -> interceptor.intercept(request(), new byte[0], (request, body) -> okResponse(executions)))
                .isInstanceOf(OutboundRateLimitException.class);

        clock.addAndGet(1_000_000_000L);

        interceptor.intercept(request(), new byte[0], (request, body) -> okResponse(executions));
        assertThat(executions.get()).isEqualTo(2);
    }

    private MockClientHttpRequest request() {
        return new MockClientHttpRequest(HttpMethod.POST, URI.create("https://example.com/v1/payments/confirm"));
    }

    private MockClientHttpResponse okResponse(AtomicInteger executions) {
        executions.incrementAndGet();
        return new MockClientHttpResponse("{}".getBytes(StandardCharsets.UTF_8), HttpStatus.OK);
    }
}
