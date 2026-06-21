package roomescape.infra.toss;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;
import roomescape.global.ratelimit.TokenBucketRateLimiter;

import java.io.IOException;

@Component
public class OutboundRateLimitInterceptor implements ClientHttpRequestInterceptor {

    private final TokenBucketRateLimiter outboundRateLimiter;

    public OutboundRateLimitInterceptor(@Qualifier("outboundRateLimiter") TokenBucketRateLimiter outboundRateLimiter) {
        this.outboundRateLimiter = outboundRateLimiter;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (!outboundRateLimiter.tryConsume()) {
            throw new CustomException(ErrorCode.OUTBOUND_RATE_LIMIT_EXCEEDED);
        }
        return execution.execute(request, body);
    }
}
