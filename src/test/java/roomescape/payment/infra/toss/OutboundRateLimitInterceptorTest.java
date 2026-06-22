package roomescape.payment.infra.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.ratelimit.TokenBucketRateLimiter;

class OutboundRateLimitInterceptorTest {

    @Test
    void 자체_한도를_넘으면_외부로_보내지_않고_보충_후에는_다시_보낸다() throws IOException {
        AtomicLong clock = new AtomicLong();
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1, clock::get);
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(limiter);
        AtomicInteger externalCalls = new AtomicInteger();
        ClientHttpResponse success = mock(ClientHttpResponse.class);
        ClientHttpRequestExecution execution = (request, body) -> {
            externalCalls.incrementAndGet();
            return success;
        };
        HttpRequest request = mock(HttpRequest.class);

        assertThat(interceptor.intercept(request, new byte[0], execution)).isSameAs(success);
        assertThatThrownBy(() -> interceptor.intercept(request, new byte[0], execution))
                .isInstanceOf(OutboundRateLimitException.class);
        assertThat(externalCalls).hasValue(1);

        clock.addAndGet(Duration.ofSeconds(1).toNanos());

        assertThat(interceptor.intercept(request, new byte[0], execution)).isSameAs(success);
        assertThat(externalCalls).hasValue(2);
    }
}
