package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import roomescape.ratelimit.TokenBucketRateLimiter;

class OutboundRateLimitInterceptorTest {

    @Test
    @DisplayName("나가는 호출 전 토큰을 소비하고, 토큰이 없으면 외부로 보내지 않는다.")
    void rejectsBeforeSendingWhenTokenIsEmpty() throws IOException {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 1.0, () -> 0);
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(rateLimiter);
        AtomicInteger executions = new AtomicInteger();
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.POST,
                "https://api.tosspayments.com/v1/payments/confirm");

        interceptor.intercept(request, new byte[0], (req, body) -> {
            executions.incrementAndGet();
            return new MockClientHttpResponse(new byte[0], 200);
        });

        assertThatThrownBy(() -> interceptor.intercept(request, new byte[0], (req, body) -> {
            executions.incrementAndGet();
            return new MockClientHttpResponse(new byte[0], 200);
        })).isInstanceOf(OutboundRateLimitException.class);
        assertThat(executions).hasValue(1);
    }

}
