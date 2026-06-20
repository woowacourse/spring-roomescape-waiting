package roomescape.pg.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpResponse;
import roomescape.global.web.ratelimit.TokenBucketRateLimiter;

class OutboundRateLimitInterceptorTest {

    @Test
    void 나가는_호출이_한도를_초과하면_외부로_보내지_않는다() throws IOException {
        AtomicLong clock = new AtomicLong();
        AtomicInteger requestCount = new AtomicInteger();
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1.0, clock::get)
        );
        ClientHttpRequestExecution execution = (request, body) -> {
            requestCount.incrementAndGet();
            return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
        };

        interceptor.intercept(request(), new byte[0], execution);

        assertThatThrownBy(() -> interceptor.intercept(request(), new byte[0], execution))
                .isInstanceOf(OutboundRateLimitException.class);
        assertThat(requestCount).hasValue(1);
    }

    private HttpRequest request() {
        return new HttpRequest() {
            @Override
            public HttpMethod getMethod() {
                return HttpMethod.POST;
            }

            @Override
            public URI getURI() {
                return URI.create("/v1/payments/confirm");
            }

            @Override
            public org.springframework.http.HttpHeaders getHeaders() {
                return new org.springframework.http.HttpHeaders();
            }

            @Override
            public Map<String, Object> getAttributes() {
                return Map.of();
            }
        };
    }
}
