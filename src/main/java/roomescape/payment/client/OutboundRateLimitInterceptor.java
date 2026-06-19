package roomescape.payment.client;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.infra.ratelimit.TokenBucket;

class OutboundRateLimitInterceptor implements ClientHttpRequestInterceptor {

    private final TokenBucket tokenBucket;

    OutboundRateLimitInterceptor(TokenBucket tokenBucket) {
        this.tokenBucket = tokenBucket;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        if (!tokenBucket.tryConsume()) {
            throw new OutboundRateLimitException();
        }
        return execution.execute(request, body);
    }
}