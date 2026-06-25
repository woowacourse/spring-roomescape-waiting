package roomescape.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 나가는(egress) 호출 한도와 토스 429 재시도 정책. 들어오는 한도(rate-limit.*)와 분리해 외부화한다.
 */
@ConfigurationProperties(prefix = "outbound-rate-limit")
public record OutboundRateLimitProperties(
        long capacity,
        double refillPerSec,
        int maxAttempts,
        long fallbackSeconds
) {
}
