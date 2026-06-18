package roomescape.global.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RateLimitBuckets {

    Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    private final int capacity;
    private final double refillPerSecond;
    private final NanoClock nanoClock;

    public RateLimitBuckets(
            @Value("${ratelimit.capacity:10}") int capacity,
            @Value("${ratelimit.refillPerSecond:100}") double refillPerSecond,
            NanoClock nanoClock
    ) {
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
        this.nanoClock = nanoClock;
    }

    public RateLimitBucket getOrCreateByKey(String key) {
        return buckets.computeIfAbsent(key, k -> {
            return new RateLimitBucket(
                    capacity,
                    refillPerSecond,
                    nanoClock
            );
        });
    }
}
