package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;

class OutboundRateLimitInterceptorTest {

    @Test
    void 아웃바운드_한도를_초과하면_외부_호출을_보내지_않는다() throws Exception {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 0, () -> 0L);
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(rateLimiter);
        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        byte[] body = new byte[0];

        assertThatThrownBy(() -> {
            interceptor.intercept(request, body, execution);
            interceptor.intercept(request, body, execution);
        }).isInstanceOf(OutboundRateLimitException.class);

        verify(execution, times(1)).execute(request, body);
    }
}
