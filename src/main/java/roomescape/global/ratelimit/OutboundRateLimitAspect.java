package roomescape.global.ratelimit;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class OutboundRateLimitAspect {

    private final RateLimiters rateLimiters;

    @Around("@annotation(outboundRateLimit)")
    public Object applyRateLimit(ProceedingJoinPoint joinPoint, OutboundRateLimit outboundRateLimit) throws Throwable {
        RateLimitBucket bucket = rateLimiters.getBucket(RateLimitType.OUTBOUND, outboundRateLimit.key());

        if (!bucket.tryConsume()) {
            throw new RateLimitException("아웃바운드 요청 속도 제한 초과. 재시도 시간: " + bucket.retryAfterSeconds() + "초");
        }

        return joinPoint.proceed();
    }
}
