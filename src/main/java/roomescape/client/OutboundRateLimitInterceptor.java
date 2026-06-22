package roomescape.client;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.ratelimit.TokenBucketRateLimiter;

/**
 * 나가는(outbound) 호출에 거는 Rate Limit 인터셉터(클라이언트 관점). 들어오는 쪽(서버 관점)과 똑같은
 * {@link TokenBucketRateLimiter} 알고리즘을 방향만 바꿔 재사용한다.
 *
 * <p>외부 호출 '전에' 토큰을 소비해, 한도를 넘으면 외부로 보내지 않고 {@link OutboundRateLimitException} 으로 거부한다.
 */
public class OutboundRateLimitInterceptor implements ClientHttpRequestInterceptor {

    private final TokenBucketRateLimiter rateLimiter;

    public OutboundRateLimitInterceptor(TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        if (!rateLimiter.tryConsume()) {
            throw new OutboundRateLimitException("외부로 나가는 요청의 한도를 넘었습니다.");
        }
        return execution.execute(request, body);
    }
}
