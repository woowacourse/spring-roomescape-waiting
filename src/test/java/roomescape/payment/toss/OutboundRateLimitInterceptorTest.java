package roomescape.payment.toss;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.exception.OutboundRateLimitException;
import roomescape.ratelimit.TokenBucketRateLimiter;

import java.io.IOException;
import java.util.function.LongSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboundRateLimitInterceptorTest {

    private final LongSupplier frozenClock = () -> 0L;

    @Test
    void 토큰이_있으면_실제_호출로_위임한다() throws IOException {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1, frozenClock);
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(limiter);
        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        byte[] body = new byte[0];
        when(execution.execute(any(), any())).thenReturn(response);

        ClientHttpResponse result = interceptor.intercept(request, body, execution);

        assertThat(result).isSameAs(response);
        verify(execution).execute(request, body);
    }

    @Test
    void 토큰이_없으면_외부로_호출하지_않고_OutboundRateLimitException을_던진다() throws IOException {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1, frozenClock);
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(limiter);
        interceptor.intercept(mock(HttpRequest.class), new byte[0], mock(ClientHttpRequestExecution.class));

        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);

        assertThatThrownBy(() -> interceptor.intercept(request, new byte[0], execution))
                .isInstanceOf(OutboundRateLimitException.class);
        verify(execution, never()).execute(any(), any());
    }
}
