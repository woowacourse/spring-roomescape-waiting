package roomescape.ratelimit;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class OutboundRateLimitInterceptor implements ClientHttpRequestInterceptor {

    private final TokenBucketRateLimiter rateLimiter;

    public OutboundRateLimitInterceptor(TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        if (!rateLimiter.tryConsume()) {
            throw new OutboundRateLimitException("아웃바운드 Rate Limit 초과 — 토스로 요청을 보내지 않음",
                    rateLimiter.retryAfterSeconds());
        }
        return execution.execute(request, body);
    }
}
