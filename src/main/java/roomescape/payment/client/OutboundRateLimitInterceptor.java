package roomescape.payment.client;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.ratelimit.TokenBucketRateLimiter;

/**
 * 나가는 결제 호출에 토큰 버킷 Rate Limit 을 적용하는 인터셉터.
 *
 * <p>외부로 보내기 전에 토큰을 소비해, 한도를 넘으면 토스에 보내지 않고 {@link OutboundRateLimitException}
 * 으로 거부한다. 들어오는 쪽과 같은 {@link TokenBucketRateLimiter} 를 방향만 바꿔 재사용한다.
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
            throw new OutboundRateLimitException("결제 요청이 많아 처리하지 못했습니다. 잠시 후 다시 시도해주세요.");
        }
        return execution.execute(request, body);
    }
}
