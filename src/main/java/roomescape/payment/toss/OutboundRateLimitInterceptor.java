package roomescape.payment.toss;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.exception.OutboundRateLimitException;
import roomescape.ratelimit.TokenBucketRateLimiter;

import java.io.IOException;

public class OutboundRateLimitInterceptor implements ClientHttpRequestInterceptor {

    private final TokenBucketRateLimiter rateLimiter;

    public OutboundRateLimitInterceptor(TokenBucketRateLimiter outboundRateLimiter) {
        this.rateLimiter = outboundRateLimiter;
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
