package roomescape.client;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.global.ratelimit.TokenBucketRateLimiter;

/**
 * 나가는(outbound) 호출에 거는 Rate Limit 인터셉터. 외부 호출 '전에' 토큰을 소비해, 한도를 넘으면 보내지 않고
 * {@link TossOutboundRateLimitException} 으로 거부한다. 들어오는 쪽과 같은 {@link TokenBucketRateLimiter}를 재사용한다.
 */
public class OutboundRateLimitInterceptor implements ClientHttpRequestInterceptor {

    private final TokenBucketRateLimiter rateLimiter;

    public OutboundRateLimitInterceptor(TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (!rateLimiter.tryConsume()) {
            throw new TossOutboundRateLimitException();
        }
        return execution.execute(request, body);
    }
}
