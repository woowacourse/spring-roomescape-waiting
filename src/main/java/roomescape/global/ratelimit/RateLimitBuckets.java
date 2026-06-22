package roomescape.global.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 키별 토큰 버킷을 보관하는 순수 일급 컬렉션.
 *
 * 어떤 종류(인바운드/아웃바운드)인지는 알지 못한다. 생성 시 주어진 한도(capacity·refillPerSecond)로
 * 키마다 버킷을 만들어 캐싱할 뿐이며, 종류별 구분과 설정 주입은 {@link RateLimiters}가 담당한다.
 */
public class RateLimitBuckets {

    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();
    private final int capacity;
    private final double refillPerSecond;
    private final NanoClock nanoClock;

    public RateLimitBuckets(int capacity, double refillPerSecond, NanoClock nanoClock) {
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
        this.nanoClock = nanoClock;
    }

    public RateLimitBucket getOrCreateByKey(String key) {
        return buckets.computeIfAbsent(key, k -> new RateLimitBucket(capacity, refillPerSecond, nanoClock));
    }
}
