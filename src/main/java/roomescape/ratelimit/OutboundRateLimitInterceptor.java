package roomescape.ratelimit;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OutboundRateLimitInterceptor implements ClientHttpRequestInterceptor {

    private final TokenBucketRateLimiter rateLimiter;

    public OutboundRateLimitInterceptor(
            @Value("${outbound-rate-limit.capacity}") long capacity,
            @Value("${outbound-rate-limit.refill-per-sec}") double refillPerSec) {
        this.rateLimiter = new TokenBucketRateLimiter(capacity, refillPerSec, System::nanoTime);
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        if (!rateLimiter.tryConsume()) {
            throw new OutboundRateLimitException(
                    "나가는 호출이 한도를 초과했습니다. 잠시 후 다시 시도해 주세요.", rateLimiter.retryAfterSeconds());
        }
        return execution.execute(request, body);
    }
}
