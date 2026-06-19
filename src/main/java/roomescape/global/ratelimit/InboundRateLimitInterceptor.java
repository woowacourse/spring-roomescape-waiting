package roomescape.global.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class InboundRateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiters rateLimiters;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        if (handler instanceof HandlerMethod handlerMethod) {
            InboundRateLimit rateLimit = findRateLimit(handlerMethod);
            if (rateLimit == null) {
                return true;
            }
            RateLimitBucket bucket = rateLimiters.getBucket(RateLimitType.INBOUND, rateLimit.key());

            if (!bucket.tryConsume()) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(bucket.retryAfterSeconds()));
                return false;
            }
        }

        return true;
    }

    private InboundRateLimit findRateLimit(HandlerMethod handler) {
        InboundRateLimit rateLimit = handler.getMethodAnnotation(InboundRateLimit.class);
        if (rateLimit != null) {
            return rateLimit;
        }

        return handler.getBeanType().getAnnotation(InboundRateLimit.class);
    }
}
