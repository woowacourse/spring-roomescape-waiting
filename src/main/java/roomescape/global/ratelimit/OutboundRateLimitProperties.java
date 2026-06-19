package roomescape.global.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 나가는 호출(아웃바운드)의 레이트 리밋 설정. (outbound-rate-limit.*)
 * 값이 없으면 {@link RateLimitType#OUTBOUND}의 종류별 기본값으로 폴백한다.
 */
@ConfigurationProperties("outbound-rate-limit")
public record OutboundRateLimitProperties(Integer capacity, Double refillPerSecond) {
}
