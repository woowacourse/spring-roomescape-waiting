package roomescape.global.ratelimit;

import java.util.EnumMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 종류별 레이트 리밋 설정값을 외부 설정에서 주입받는다. (rate-limit.limits.&lt;type&gt;.*)
 * 값이 일부만 주어지면 비어 있는 항목은 {@link RateLimitType}의 종류별 기본값으로 폴백한다.
 */
@ConfigurationProperties("rate-limit")
public class RateLimitProperties {

    private final Map<RateLimitType, Limit> limits = new EnumMap<>(RateLimitType.class);

    public Map<RateLimitType, Limit> getLimits() {
        return limits;
    }

    public record Limit(Integer capacity, Double refillPerSecond) {
    }
}
