package roomescape.common.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 들어오는 요청 Rate Limit 설정(서버 관점). capacity: 허용 버스트, refillPerSec: 평균 초당 처리 상한.
 * 값만 바꾸면 코드 수정 없이 거부 시점이 달라진다(나가는 쪽은 outbound-rate-limit.*로 분리).
 */
@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(long capacity, double refillPerSec) {
}
