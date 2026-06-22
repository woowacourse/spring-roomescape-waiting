package roomescape.global.ratelimit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboundRateLimitAspectTest {

    @Mock
    private RateLimiters rateLimiters;

    @Mock
    private RateLimitBucket bucket;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private OutboundRateLimit outboundRateLimit;

    @InjectMocks
    private OutboundRateLimitAspect aspect;

    private static final String LIMIT_KEY = "test-key";

    @Test
    void 토큰이_충분할_경우_메서드를_정상적으로_실행한다() throws Throwable {
        // given
        when(outboundRateLimit.key()).thenReturn(LIMIT_KEY);
        when(rateLimiters.getBucket(RateLimitType.OUTBOUND, LIMIT_KEY)).thenReturn(bucket);
        when(bucket.tryConsume()).thenReturn(true);

        // when
        aspect.applyRateLimit(joinPoint, outboundRateLimit);

        // then
        verify(joinPoint).proceed();
    }

    @Test
    void 토큰이_부족할_경우_RateLimitException을_던지고_메서드를_실행하지_않는다() throws Throwable {
        // given
        when(outboundRateLimit.key()).thenReturn(LIMIT_KEY);
        when(rateLimiters.getBucket(RateLimitType.OUTBOUND, LIMIT_KEY)).thenReturn(bucket);
        when(bucket.tryConsume()).thenReturn(false);
        when(bucket.retryAfterSeconds()).thenReturn(5L);

        // when and then
        assertThatThrownBy(() -> aspect.applyRateLimit(joinPoint, outboundRateLimit))
                .isInstanceOf(RateLimitException.class)
                .hasMessageContaining("아웃바운드 요청 속도 제한 초과");

        verify(joinPoint, never()).proceed();
    }
}
