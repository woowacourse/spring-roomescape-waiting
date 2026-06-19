package roomescape.global.ratelimit;

import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 레이트 리밋의 진입점.
 *
 * 종류({@link RateLimitType})와 종류별 기본값을 알고 있으며, 종류마다 독립된
 * {@link RateLimitBuckets}를 그에 맞는 한도로 생성해 보관한다.
 * 공통 로직({@link RateLimitBuckets}, {@link RateLimitBucket})은 종류를 알지 못한 채 그대로 재사용된다.
 */
@Component
public class RateLimiters {

    private final Map<RateLimitType, RateLimitBuckets> bucketsByType;

    public RateLimiters(RateLimitProperties properties, NanoClock nanoClock) {
        this.bucketsByType = new EnumMap<>(RateLimitType.class);
        for (RateLimitType type : RateLimitType.values()) {
            bucketsByType.put(type, createBuckets(type, properties, nanoClock));
        }
    }

    private RateLimitBuckets createBuckets(RateLimitType type, RateLimitProperties properties, NanoClock nanoClock) {
        RateLimitProperties.Limit override = properties.getLimits().get(type);

        return new RateLimitBuckets(
                resolveCapacity(type, override),
                resolveRefillPerSecond(type, override),
                nanoClock
        );
    }

    private int resolveCapacity(RateLimitType type, RateLimitProperties.Limit override) {
        if (override != null && override.capacity() != null) {
            return override.capacity();
        }
        return type.defaultCapacity();
    }

    private double resolveRefillPerSecond(RateLimitType type, RateLimitProperties.Limit override) {
        if (override != null && override.refillPerSecond() != null) {
            return override.refillPerSecond();
        }
        return type.defaultRefillPerSecond();
    }

    public RateLimitBucket getBucket(RateLimitType type, String key) {
        return bucketsByType.get(type).getOrCreateByKey(key);
    }
}
