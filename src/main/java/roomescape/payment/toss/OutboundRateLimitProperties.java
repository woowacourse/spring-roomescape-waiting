package roomescape.payment.toss;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 나가는 토스 호출 Rate Limit 설정(클라이언트 관점). 들어오는 쪽(rate-limit.*)과 한도를 분리해 외부화한다.
 * capacity: 허용 버스트, refillPerSec: 평균 초당 호출 상한.
 */
@ConfigurationProperties(prefix = "outbound-rate-limit")
public record OutboundRateLimitProperties(long capacity, double refillPerSec) {
}
