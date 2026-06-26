package roomescape.payment.infrastructure.toss;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.common.ratelimit.TokenBucketRateLimiter;
import roomescape.payment.domain.exception.OutboundRateLimitException;

/**
 * 나가는 호출 Rate Limit. 호출 전에 토큰을 소비해, 한도를 넘으면 외부로 보내지 않고 도메인 예외로 거부한다.
 */
public class OutboundRateLimitInterceptor implements ClientHttpRequestInterceptor {

    private final TokenBucketRateLimiter rateLimiter;

    public OutboundRateLimitInterceptor(TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (!rateLimiter.tryConsume()) {
            throw new OutboundRateLimitException();
        }
        return execution.execute(request, body);
    }
}
