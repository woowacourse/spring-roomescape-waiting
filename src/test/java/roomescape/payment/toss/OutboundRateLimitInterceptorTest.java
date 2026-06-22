package roomescape.payment.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import roomescape.payment.OutboundRateLimitException;
import roomescape.ratelimit.TokenBucketRateLimiter;

class OutboundRateLimitInterceptorTest {

    private final AtomicLong now = new AtomicLong();

    @Test
    void 토큰이_있으면_외부_호출을_보낸다() throws IOException {
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1.0, now::get));
        AtomicInteger executions = new AtomicInteger();

        var response = interceptor.intercept(new MockClientHttpRequest(HttpMethod.POST, "/"), new byte[0],
                (request, body) -> {
                    executions.incrementAndGet();
                    return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(executions.get()).isEqualTo(1);
    }

    @Test
    void 토큰이_없으면_외부로_보내지_않고_거부한다() throws IOException {
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1.0, now::get));
        AtomicInteger executions = new AtomicInteger();
        interceptor.intercept(new MockClientHttpRequest(HttpMethod.POST, "/"), new byte[0],
                (request, body) -> {
                    executions.incrementAndGet();
                    return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
                });

        assertThatThrownBy(() -> interceptor.intercept(new MockClientHttpRequest(HttpMethod.POST, "/"), new byte[0],
                (request, body) -> {
                    executions.incrementAndGet();
                    return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
                }))
                .isInstanceOf(OutboundRateLimitException.class)
                .hasMessage("토스 결제 승인 요청 한도를 초과해 외부로 보내지 않았습니다.");
        assertThat(executions.get()).isEqualTo(1);
    }

    @Test
    void 토큰이_보충되면_다시_외부_호출을_보낸다() throws IOException {
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1.0, now::get));
        AtomicInteger executions = new AtomicInteger();
        interceptor.intercept(new MockClientHttpRequest(HttpMethod.POST, "/"), new byte[0],
                (request, body) -> {
                    executions.incrementAndGet();
                    return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
                });

        now.addAndGet(1_000_000_000L);
        interceptor.intercept(new MockClientHttpRequest(HttpMethod.POST, "/"), new byte[0],
                (request, body) -> {
                    executions.incrementAndGet();
                    return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
                });

        assertThat(executions.get()).isEqualTo(2);
    }
}
