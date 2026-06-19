package roomescape.reservation.infra.toss;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.global.exception.OutboundRateLimitException;
import roomescape.global.ratelimit.TokenBucketRateLimiter;

/**
 * Toss로 나가는 호출 전에 outbound 전용 토큰을 소비해 자체 호출량을 제한합니다.
 */
public class OutboundRateLimitInterceptor implements ClientHttpRequestInterceptor {

    private final TokenBucketRateLimiter rateLimiter;

    public OutboundRateLimitInterceptor(TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        if (!rateLimiter.tryConsume()) {
            throw new OutboundRateLimitException();
        }
        return execution.execute(request, body);
    }
}
