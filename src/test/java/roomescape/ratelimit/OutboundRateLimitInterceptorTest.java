package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import roomescape.exception.server.OutboundRateLimitException;

class OutboundRateLimitInterceptorTest {

    @Test
    void 토큰이_있으면_외부_요청을_보낸다() throws Exception {
        AtomicLong clock = new AtomicLong(0);
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1.0, clock::get)
        );
        AtomicBoolean executed = new AtomicBoolean(false);

        var response = interceptor.intercept(
                new MockClientHttpRequest(HttpMethod.POST, "/v1/payments/confirm"),
                new byte[0],
                (request, body) -> {
                    executed.set(true);
                    return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
                }
        );

        assertThat(executed).isTrue();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void 토큰이_없으면_외부_요청을_보내지_않고_예외를_던진다() throws Exception {
        AtomicLong clock = new AtomicLong(0);
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1.0, clock::get)
        );
        interceptor.intercept(
                new MockClientHttpRequest(HttpMethod.POST, "/v1/payments/confirm"),
                new byte[0],
                (request, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK)
        );
        AtomicBoolean executed = new AtomicBoolean(false);

        assertThatThrownBy(() -> interceptor.intercept(
                new MockClientHttpRequest(HttpMethod.POST, "/v1/payments/confirm"),
                new byte[0],
                (request, body) -> {
                    executed.set(true);
                    return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
                }
        )).isInstanceOf(OutboundRateLimitException.class);
        assertThat(executed).isFalse();
    }
}
