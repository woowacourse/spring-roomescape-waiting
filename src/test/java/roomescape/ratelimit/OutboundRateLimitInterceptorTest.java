package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

class OutboundRateLimitInterceptorTest {

    @Test
    void rejectsBeforeOutboundRequestWhenLocalBucketIsEmptyTest() throws IOException {
        AtomicLong now = new AtomicLong(0L);
        OutboundRateLimitProperties properties = new OutboundRateLimitProperties(true, 1, 0.2D, 3);
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(properties,
                new TokenBucket(properties.capacity(), properties.refillPerSecond(), now::get));
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.POST, URI.create("https://example.com"));
        CountingExecution execution = new CountingExecution();

        interceptor.intercept(request, new byte[0], execution);

        assertThatThrownBy(() -> interceptor.intercept(request, new byte[0], execution))
                .isInstanceOf(OutboundRateLimitException.class);
        assertThat(execution.count()).isEqualTo(1);
    }

    @Test
    void skipsLocalLimitWhenDisabledTest() throws IOException {
        AtomicLong now = new AtomicLong(0L);
        OutboundRateLimitProperties properties = new OutboundRateLimitProperties(false, 1, 0.2D, 3);
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(properties,
                new TokenBucket(properties.capacity(), properties.refillPerSecond(), now::get));
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.POST, URI.create("https://example.com"));
        CountingExecution execution = new CountingExecution();

        interceptor.intercept(request, new byte[0], execution);
        interceptor.intercept(request, new byte[0], execution);

        assertThat(execution.count()).isEqualTo(2);
    }

    private static class CountingExecution implements ClientHttpRequestExecution {

        private int count;

        @Override
        public ClientHttpResponse execute(org.springframework.http.HttpRequest request, byte[] body) {
            count++;
            return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
        }

        private int count() {
            return count;
        }
    }
}
