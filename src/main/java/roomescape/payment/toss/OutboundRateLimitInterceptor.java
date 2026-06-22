package roomescape.payment.toss;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.payment.OutboundRateLimitException;
import roomescape.ratelimit.TokenBucketRateLimiter;

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
            throw new OutboundRateLimitException(
                    "토스 결제 승인 요청 한도를 초과해 외부로 보내지 않았습니다.",
                    rateLimiter.retryAfterSeconds()
            );
        }
        return execution.execute(request, body);
    }
}
