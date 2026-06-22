package roomescape.payment.toss;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.payment.ratelimit.TokenBucketRateLimiter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboundRateLimitInterceptorTest {

    private ClientHttpRequestExecution okExecution() throws IOException {
        var execution = mock(ClientHttpRequestExecution.class);
        var response = mock(ClientHttpResponse.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(execution.execute(any(), any())).thenReturn(response);
        return execution;
    }

    @Test
    void 한도_내_호출은_외부로_전송된다() throws IOException {
        var clock = new AtomicLong(0);
        var rateLimiter = new TokenBucketRateLimiter(2, 1.0, clock::get);
        var interceptor = new OutboundRateLimitInterceptor(rateLimiter);
        var execution = okExecution();

        interceptor.intercept(mock(HttpRequest.class), new byte[0], execution);
        interceptor.intercept(mock(HttpRequest.class), new byte[0], execution);

        verify(execution, times(2)).execute(any(), any());
    }

    @Test
    void 한도_초과_호출은_외부로_보내지_않고_예외를_던진다() throws IOException {
        var clock = new AtomicLong(0);
        var rateLimiter = new TokenBucketRateLimiter(1, 1.0, clock::get);
        var interceptor = new OutboundRateLimitInterceptor(rateLimiter);
        var execution = okExecution();

        interceptor.intercept(mock(HttpRequest.class), new byte[0], execution);

        assertThatThrownBy(() ->
                interceptor.intercept(mock(HttpRequest.class), new byte[0], execution)
        ).isInstanceOf(OutboundRateLimitException.class);

        verify(execution, times(1)).execute(any(), any());
    }

    @Test
    void 토큰이_보충되면_다시_외부로_나간다() throws IOException {
        var clock = new AtomicLong(0);
        var rateLimiter = new TokenBucketRateLimiter(1, 1.0, clock::get);
        var interceptor = new OutboundRateLimitInterceptor(rateLimiter);
        var execution = okExecution();

        interceptor.intercept(mock(HttpRequest.class), new byte[0], execution);

        assertThatThrownBy(() ->
                interceptor.intercept(mock(HttpRequest.class), new byte[0], execution)
        ).isInstanceOf(OutboundRateLimitException.class);

        clock.addAndGet(1_000_000_000L);

        interceptor.intercept(mock(HttpRequest.class), new byte[0], execution);

        verify(execution, times(2)).execute(any(), any());
    }
}
