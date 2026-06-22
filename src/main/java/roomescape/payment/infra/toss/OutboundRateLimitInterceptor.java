package roomescape.payment.infra.toss;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.global.ratelimit.TokenBucketRateLimiter;
import roomescape.payment.domain.exception.OutboundRateLimitException;

import java.io.IOException;

public class OutboundRateLimitInterceptor implements ClientHttpRequestInterceptor {
    private final TokenBucketRateLimiter rateLimiter;

    public OutboundRateLimitInterceptor(TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        if (!rateLimiter.tryConsume()) {
            throw new OutboundRateLimitException("나가는 결제 승인 호출이 자체 Rate Limit을 초과해 외부로 보내지 않았습니다.");
        }

        return execution.execute(request, body);
    }
}
