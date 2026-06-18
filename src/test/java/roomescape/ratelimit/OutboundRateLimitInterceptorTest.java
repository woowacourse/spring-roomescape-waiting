package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpResponse;
import roomescape.exception.ErrorCode;

class OutboundRateLimitInterceptorTest {

    @Test
    @DisplayName("아웃바운드 토큰이 없으면 외부 요청을 보내지 않고 즉시 거부한다.")
    void rejects_without_executing_request_when_outbound_limit_exceeded() throws IOException {
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1, () -> 0L)
        );
        AtomicInteger executionCount = new AtomicInteger();

        interceptor.intercept(request(), new byte[0], (request, body) -> {
            executionCount.incrementAndGet();
            return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
        });

        assertThatThrownBy(() -> interceptor.intercept(request(), new byte[0], (request, body) -> {
            executionCount.incrementAndGet();
            return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
        }))
                .isInstanceOf(OutboundRateLimitException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_OUTBOUND_RATE_LIMITED);
        assertThat(executionCount).hasValue(1);
    }

    private HttpRequest request() {
        return new HttpRequest() {
            @Override
            public HttpMethod getMethod() {
                return HttpMethod.POST;
            }

            @Override
            public URI getURI() {
                return URI.create("https://api.tosspayments.com/v1/payments/confirm");
            }

            @Override
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }

            @Override
            public Map<String, Object> getAttributes() {
                return Map.of();
            }
        };
    }
}
