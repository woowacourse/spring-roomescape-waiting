package roomescape.payment.toss;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.common.ratelimit.TokenBucketRateLimiter;
import roomescape.payment.exception.OutboundRateLimitException;

/**
 * 나가는 토스 호출에 Rate Limit을 거는 아웃바운드 인터셉터(클라이언트 관점). 들어오는 쪽(RateLimitInterceptor)과
 * 똑같은 TokenBucketRateLimiter를 방향만 바꿔 재사용한다 — 한도를 넘겨 호출하면 어차피 429로 거부당하니,
 * 보내기 전에 스스로 조절해 외부로 보내지 않는다.
 *
 * <p>호출 전 토큰을 소비하고, 없으면 execution을 진행하지 않고 OutboundRateLimitException으로 거부한다.
 */
public class OutboundRateLimitInterceptor implements ClientHttpRequestInterceptor {

    private final TokenBucketRateLimiter rateLimiter;

    public OutboundRateLimitInterceptor(TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        if (!rateLimiter.tryConsume()) {
            throw new OutboundRateLimitException("나가는 결제 호출이 한도를 초과해 외부로 보내지 않았습니다.");
        }
        return execution.execute(request, body);
    }
}
