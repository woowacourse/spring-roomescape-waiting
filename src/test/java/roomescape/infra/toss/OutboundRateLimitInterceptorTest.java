package roomescape.infra.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import roomescape.ratelimit.TokenBucketRateLimiter;

class OutboundRateLimitInterceptorTest {

    private static final byte[] BODY = "{}".getBytes(StandardCharsets.UTF_8);

    @Test
    @DisplayName("토큰이 있으면 외부 요청을 보낸다.")
    void executeWhenTokenIsAvailable() throws Exception {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 1, now::get);
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(rateLimiter);
        AtomicInteger executions = new AtomicInteger();
        ClientHttpRequestExecution execution = (request, body) -> {
            executions.incrementAndGet();
            return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
        };

        ClientHttpResponse response = interceptor.intercept(request(), BODY, execution);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(executions).hasValue(1);
    }

    @Test
    @DisplayName("토큰이 없으면 외부 요청을 보내지 않고 예외를 던진다.")
    void rejectBeforeExternalRequestWhenTokenIsNotAvailable() throws Exception {
        AtomicLong now = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 1, now::get);
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(rateLimiter);
        AtomicInteger executions = new AtomicInteger();
        ClientHttpRequestExecution execution = (request, body) -> {
            executions.incrementAndGet();
            return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
        };

        interceptor.intercept(request(), BODY, execution);

        assertThatThrownBy(() -> interceptor.intercept(request(), BODY, execution))
                .isInstanceOf(OutboundRateLimitException.class)
                .hasMessage("토스 요청 한도를 초과했습니다.");
        assertThat(executions).hasValue(1);
    }

    private MockClientHttpRequest request() {
        return new MockClientHttpRequest(HttpMethod.POST, URI.create("/v1/payments/confirm"));
    }
}
