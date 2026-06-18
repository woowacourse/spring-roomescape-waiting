package roomescape.client.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import roomescape.ratelimit.TokenBucketRateLimiter;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpStatus.OK;

class OutboundRateLimitInterceptorTest {

    @Test
    void 토큰이_없으면_외부로_요청을_보내지_않는다() throws IOException {
        FakeNanoClock clock = new FakeNanoClock();
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1, clock::now)
        );
        AtomicInteger executionCount = new AtomicInteger();
        ClientHttpRequestExecution execution = (request, body) -> {
            executionCount.incrementAndGet();
            return new MockClientHttpResponse(new byte[0], OK);
        };

        interceptor.intercept(request(), new byte[0], execution);

        assertThatThrownBy(() -> interceptor.intercept(request(), new byte[0], execution))
                .isInstanceOf(OutboundRateLimitException.class);
        assertThat(executionCount).hasValue(1);
    }

    @Test
    void 토큰이_보충되면_다시_외부_요청을_보낼_수_있다() throws IOException {
        FakeNanoClock clock = new FakeNanoClock();
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1, clock::now)
        );
        AtomicInteger executionCount = new AtomicInteger();
        ClientHttpRequestExecution execution = (request, body) -> {
            executionCount.incrementAndGet();
            return new MockClientHttpResponse(new byte[0], OK);
        };

        interceptor.intercept(request(), new byte[0], execution);
        clock.advanceSeconds(1);
        ClientHttpResponse response = interceptor.intercept(request(), new byte[0], execution);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(executionCount).hasValue(2);
    }

    private MockClientHttpRequest request() {
        return new MockClientHttpRequest(HttpMethod.POST, URI.create("https://example.com"));
    }

    private static class FakeNanoClock {

        private long now;

        long now() {
            return now;
        }

        void advanceSeconds(long seconds) {
            now += seconds * 1_000_000_000L;
        }
    }
}
