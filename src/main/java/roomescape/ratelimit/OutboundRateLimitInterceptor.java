package roomescape.ratelimit;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class OutboundRateLimitInterceptor implements ClientHttpRequestInterceptor {

    private final OutboundRateLimitProperties properties;
    private final TokenBucket rateLimiter;

    public OutboundRateLimitInterceptor(OutboundRateLimitProperties properties, TokenBucket rateLimiter) {
        this.properties = properties;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        consumeToken();
        return execution.execute(request, body);
    }

    public void consumeToken() {
        if (properties.enabled() && !rateLimiter.tryConsume()) {
            throw new OutboundRateLimitException("Outbound rate limit exceeded before calling Toss");
        }
    }
}
