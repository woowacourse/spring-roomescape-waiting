package roomescape.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OutboundRateLimitProperties {

    private final long capacity;
    private final double refillPerSecond;

    public OutboundRateLimitProperties(
            @Value("${outbound-rate-limit.capacity:100}") long capacity,
            @Value("${outbound-rate-limit.refill-per-second:100}") double refillPerSecond
    ) {
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
    }

    public long getCapacity() {
        return capacity;
    }

    public double getRefillPerSecond() {
        return refillPerSecond;
    }
}
