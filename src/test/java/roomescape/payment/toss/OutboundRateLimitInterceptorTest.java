package roomescape.payment.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import roomescape.common.ratelimit.TokenBucketRateLimiter;
import roomescape.payment.exception.OutboundRateLimitException;

/**
 * 나가는 호출 Rate Limit 인터셉터가 토큰 유무에 따라 실제 전송을 진행/차단하는지 검증한다.
 * 토큰이 없으면 execution을 호출하지 않고 OutboundRateLimitException으로 거부해야 한다.
 */
class OutboundRateLimitInterceptorTest {

    @Test
    void 토큰이_있으면_실제_전송을_진행한다() throws IOException {
        AtomicLong clock = new AtomicLong(0);
        AtomicInteger executed = new AtomicInteger();
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1, clock::get));

        interceptor.intercept(new MockClientHttpRequest(), new byte[0],
                (req, body) -> {
                    executed.incrementAndGet();
                    return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
                });

        assertThat(executed.get()).isEqualTo(1);
    }

    @Test
    void 토큰이_없으면_외부로_보내지_않고_OutboundRateLimitException으로_거부한다() throws IOException {
        AtomicLong clock = new AtomicLong(0);
        AtomicInteger executed = new AtomicInteger();
        OutboundRateLimitInterceptor interceptor = new OutboundRateLimitInterceptor(
                new TokenBucketRateLimiter(1, 1, clock::get));
        // 유일한 토큰을 첫 호출이 소비한다.
        interceptor.intercept(new MockClientHttpRequest(), new byte[0],
                (req, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        assertThatThrownBy(() -> interceptor.intercept(new MockClientHttpRequest(), new byte[0],
                (req, body) -> {
                    executed.incrementAndGet();
                    return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
                }))
                .isInstanceOf(OutboundRateLimitException.class);

        assertThat(executed.get()).isZero(); // 두 번째 호출은 execution이 실행되지 않았다
    }
}
