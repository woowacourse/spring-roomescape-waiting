package roomescape.ratelimit;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class OutboundRateLimitInterceptor implements ClientHttpRequestInterceptor {

    private final TokenBucketRateLimiter rateLimiter;

    @Autowired
    public OutboundRateLimitInterceptor(
        @Value("${outbound-rate-limit.capacity}") long capacity,
        @Value("${outbound-rate-limit.refill-per-sec}") double refillPerSecond
    ) {
        this(new TokenBucketRateLimiter(capacity, refillPerSecond));
    }

    OutboundRateLimitInterceptor(TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public ClientHttpResponse intercept(
        HttpRequest request,
        byte[] body,
        ClientHttpRequestExecution execution
    ) throws IOException {
        if (!rateLimiter.tryConsume()) {
            throw new OutboundRateLimitException(rateLimiter.retryAfterSeconds());
        }
        return execution.execute(request, body);
    }
}
