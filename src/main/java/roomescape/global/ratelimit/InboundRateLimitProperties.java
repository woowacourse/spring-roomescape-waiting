package roomescape.global.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 들어오는 요청(인바운드)의 레이트 리밋 설정. (rate-limit.*)
 * 값이 없으면 {@link RateLimitType#INBOUND}의 종류별 기본값으로 폴백한다.
 */
@ConfigurationProperties("rate-limit")
public record InboundRateLimitProperties(Integer capacity, Double refillPerSecond) {
}
