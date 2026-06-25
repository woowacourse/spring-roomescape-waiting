package roomescape.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.controller.dto.response.ErrorResponse;
import roomescape.exception.ErrorCode;

/**
 * 들어오는 요청에 토큰 버킷 Rate Limit 을 적용하는 인터셉터.
 *
 * <p>토큰이 없으면 컨트롤러를 호출하지 않고 429 + Retry-After 로 거부하며, 응답 본문은 앱 공통 에러 포맷
 * ({@link ErrorResponse})으로 내려준다. 쓰기(POST) 요청만 토큰을 소비하므로, 같은 경로의 조회(GET)는 한도와
 * 무관하게 통과한다.
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    private final TokenBucketRateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    public RateLimitInterceptor(TokenBucketRateLimiter rateLimiter, ObjectMapper objectMapper) {
        this.rateLimiter = rateLimiter;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (!consumesToken(request)) {
            return true;
        }
        if (rateLimiter.tryConsume()) {
            return true;
        }
        rejectWithTooManyRequests(response);
        return false;
    }

    private boolean consumesToken(HttpServletRequest request) {
        return HttpMethod.POST.matches(request.getMethod());
    }

    private void rejectWithTooManyRequests(HttpServletResponse response) throws IOException {
        ErrorCode errorCode = ErrorCode.TOO_MANY_REQUESTS;
        response.setStatus(errorCode.getStatus().value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(rateLimiter.retryAfterSeconds()));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ErrorResponse.from(errorCode, errorCode.getDetail()));
    }
}
