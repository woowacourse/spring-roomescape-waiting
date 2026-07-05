package roomescape.infrastructure.toss;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import roomescape.ratelimit.TokenBucketRateLimiter;

public class OutboundRateLimitInterceptor implements ClientHttpRequestInterceptor {
    private final TokenBucketRateLimiter rateLimiter;

    public OutboundRateLimitInterceptor(TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        if (!rateLimiter.tryConsume()) {
            throw new OutboundRateLimitException(rateLimiter.retryAfterSeconds());
        }
        return execution.execute(request, body);
    }
}
