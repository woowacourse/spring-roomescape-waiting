package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import roomescape.ratelimit.TokenBucketRateLimiter;

class OutboundRateLimitInterceptorTest {

    @Test
    @DisplayName("자체 한도를 넘기면 외부로 보내지 않고 즉시 거부한다")
    void 한도를_넘기면_외부로_보내지_않는다() throws IOException {
        AtomicLong clock = new AtomicLong(0);
        OutboundRateLimitInterceptor interceptor =
                new OutboundRateLimitInterceptor(new TokenBucketRateLimiter(2, 1.0, clock::get));
        CountingExecution execution = new CountingExecution();

        interceptor.intercept(request(), new byte[0], execution);
        interceptor.intercept(request(), new byte[0], execution);
        assertThat(execution.count()).isEqualTo(2);

        assertThatThrownBy(() -> interceptor.intercept(request(), new byte[0], execution))
                .isInstanceOf(OutboundRateLimitException.class);
        assertThat(execution.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("토큰이 보충되면 다시 외부로 나간다")
    void 토큰이_보충되면_다시_나간다() throws IOException {
        AtomicLong clock = new AtomicLong(0);
        OutboundRateLimitInterceptor interceptor =
                new OutboundRateLimitInterceptor(new TokenBucketRateLimiter(1, 1.0, clock::get));
        CountingExecution execution = new CountingExecution();

        interceptor.intercept(request(), new byte[0], execution);
        assertThatThrownBy(() -> interceptor.intercept(request(), new byte[0], execution))
                .isInstanceOf(OutboundRateLimitException.class);

        clock.addAndGet(1_000_000_000L);

        interceptor.intercept(request(), new byte[0], execution);
        assertThat(execution.count()).isEqualTo(2);
    }

    private MockClientHttpRequest request() {
        return new MockClientHttpRequest(HttpMethod.POST, URI.create("/v1/payments/confirm"));
    }

    private static final class CountingExecution implements ClientHttpRequestExecution {

        private final AtomicInteger count = new AtomicInteger();

        @Override
        public ClientHttpResponse execute(HttpRequest request, byte[] body) {
            count.incrementAndGet();
            return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
        }

        private int count() {
            return count.get();
        }
    }
}