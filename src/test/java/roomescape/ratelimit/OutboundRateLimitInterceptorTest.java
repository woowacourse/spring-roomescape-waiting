package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;

class OutboundRateLimitInterceptorTest {

    @Test
    void 자체_한도를_초과하면_외부_호출을_보내지_않는다() throws Exception {
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(
            new TokenBucketRateLimiter(1, 1, () -> 0L)
        );
        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        byte[] body = new byte[0];

        interceptor.intercept(request, body, execution);

        assertThatThrownBy(() -> interceptor.intercept(request, body, execution))
            .isInstanceOf(OutboundRateLimitException.class);
        verify(execution, times(1)).execute(request, body);
    }

    @Test
    void 토큰이_보충되면_외부_호출을_다시_허용한다() throws Exception {
        AtomicLong clock = new AtomicLong();
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(
            new TokenBucketRateLimiter(1, 1, clock::get)
        );
        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        byte[] body = new byte[0];

        interceptor.intercept(request, body, execution);
        assertThatThrownBy(() -> interceptor.intercept(request, body, execution))
            .isInstanceOf(OutboundRateLimitException.class);

        clock.addAndGet(Duration.ofSeconds(1).toNanos());
        interceptor.intercept(request, body, execution);

        verify(execution, times(2)).execute(request, body);
    }
}
