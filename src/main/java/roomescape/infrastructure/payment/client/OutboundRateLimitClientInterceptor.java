package roomescape.infrastructure.payment.client;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.exception.domain.OutboundRateLimitException;
import roomescape.ratelimit.TokenBucketRateLimiter;

public class OutboundRateLimitClientInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(OutboundRateLimitClientInterceptor.class);

    private final TokenBucketRateLimiter rateLimiter;

    public OutboundRateLimitClientInterceptor(TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (!rateLimiter.tryConsume()) {
            long retryAfterSeconds = rateLimiter.retryAfterSeconds();
            log.warn("나가는 호출 Rate Limit 초과 — 전송 차단: uri={}, retryAfter={}s",
                    request.getURI(), retryAfterSeconds);
            throw new OutboundRateLimitException();
        }
        return execution.execute(request, body);
    }
}
