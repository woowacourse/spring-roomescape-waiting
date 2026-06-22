package roomescape.payment.infrastructure.toss;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.common.ratelimit.TokenBucketRateLimiter;
import roomescape.payment.exception.OutboundRateLimitException;

import java.io.IOException;

public class TossOutboundRateLimitInterceptor implements ClientHttpRequestInterceptor {

    private final TokenBucketRateLimiter rateLimiter;

    public TossOutboundRateLimitInterceptor(final TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public ClientHttpResponse intercept(
            final HttpRequest request,
            final byte[] body,
            final ClientHttpRequestExecution execution
    ) throws IOException {
        if (rateLimiter.tryConsume()) {
            return execution.execute(request, body);
        }

        throw new OutboundRateLimitException(
                "결제 승인 요청이 일시적으로 많습니다. %d초 후 다시 시도해주세요."
                        .formatted(rateLimiter.retryAfterSeconds())
        );
    }
}
