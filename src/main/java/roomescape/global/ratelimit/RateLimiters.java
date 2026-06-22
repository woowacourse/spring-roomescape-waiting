package roomescape.global.ratelimit;

import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 레이트 리밋의 진입점.
 *
 * 종류({@link RateLimitType})와 종류별 기본값을 알고 있으며, 인바운드(rate-limit.*)·아웃바운드
 * (outbound-rate-limit.*) 설정을 각각 주입받아 종류마다 독립된 {@link RateLimitBuckets}를 생성해 보관한다.
 * 공통 로직({@link RateLimitBuckets}, {@link RateLimitBucket})은 종류를 알지 못한 채 그대로 재사용된다.
 */
@Component
public class RateLimiters {

    private final Map<RateLimitType, RateLimitBuckets> bucketsByType;

    public RateLimiters(
            InboundRateLimitProperties inboundRateLimitProperties,
            OutboundRateLimitProperties outboundRateLimitProperties,
            NanoClock nanoClock
    ) {
        this.bucketsByType = new EnumMap<>(RateLimitType.class);
        this.bucketsByType.put(RateLimitType.INBOUND, createBuckets(
                RateLimitType.INBOUND,
                inboundRateLimitProperties.capacity(),
                inboundRateLimitProperties.refillPerSecond(),
                nanoClock
        ));
        this.bucketsByType.put(RateLimitType.OUTBOUND, createBuckets(
                RateLimitType.OUTBOUND,
                outboundRateLimitProperties.capacity(),
                outboundRateLimitProperties.refillPerSecond(),
                nanoClock
        ));
    }

    private RateLimitBuckets createBuckets(RateLimitType type, Integer capacity, Double refillPerSecond, NanoClock nanoClock) {
        return new RateLimitBuckets(
                capacity != null ? capacity : type.defaultCapacity(),
                refillPerSecond != null ? refillPerSecond : type.defaultRefillPerSecond(),
                nanoClock
        );
    }

    public RateLimitBucket getBucket(RateLimitType type, String key) {
        return bucketsByType.get(type).getOrCreateByKey(key);
    }
}
