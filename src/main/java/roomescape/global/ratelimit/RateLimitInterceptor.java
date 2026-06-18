package roomescape.global.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final NanoClock nanoClock;
    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        if (handler instanceof HandlerMethod handlerMethod) {
            RateLimit rateLimit = findRateLimit(handlerMethod);
            if (rateLimit == null) {
                return true;
            }
            RateLimitBucket bucket = findOrCreateBucket(rateLimit);

            if (!bucket.tryConsume()) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(bucket.retryAfterSeconds()));
                return false;
            }
        }

        return true;
    }

    private RateLimit findRateLimit(HandlerMethod handler) {
        RateLimit rateLimit = handler.getMethodAnnotation(RateLimit.class);
        if (rateLimit != null) {
            return rateLimit;
        }

        return handler.getBeanType().getAnnotation(RateLimit.class);
    }

    private RateLimitBucket findOrCreateBucket(RateLimit rateLimit) {
        return buckets.computeIfAbsent(rateLimit.key(), key -> {
            return new RateLimitBucket(
                    rateLimit.capacity(),
                    rateLimit.refillPerSecond(),
                    nanoClock
            );
        });
    }
}
