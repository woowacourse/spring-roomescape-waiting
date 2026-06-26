package roomescape.infrastructure.toss;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;

import roomescape.ratelimit.TokenBucketRateLimiter;

class OutboundRateLimitInterceptorTest {

    @Test
    @DisplayName("나가는 호출 한도를 넘으면 외부로 보내지 않고 거부한다")
    void intercept_rateLimited() throws IOException {
        AtomicLong now = new AtomicLong(0);
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1, now::get)
        );
        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);

        interceptor.intercept(request, new byte[0], execution);

        assertThatThrownBy(() -> interceptor.intercept(request, new byte[0], execution))
                .isInstanceOf(OutboundRateLimitException.class)
                .hasMessageContaining("한도");
        verify(execution, times(1)).execute(any(), any());
    }
}
