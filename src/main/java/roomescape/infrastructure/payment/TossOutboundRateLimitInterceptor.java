package roomescape.infrastructure.payment;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.infrastructure.ratelimiter.TokenBucket;

public class TossOutboundRateLimitInterceptor implements ClientHttpRequestInterceptor {
    private final TokenBucket tokenBucket;

    public TossOutboundRateLimitInterceptor(TokenBucket tokenBucket) {
        this.tokenBucket = tokenBucket;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (!tokenBucket.tryConsume()) {
            throw new OutboundRateLimitException(tokenBucket.retryAfterSeconds());
        }
        return execution.execute(request, body);
    }
}
