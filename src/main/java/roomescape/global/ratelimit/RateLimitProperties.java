package roomescape.global.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {
    private long capacity = 100;
    private double refillPerSec = 100.0;

    public long capacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public double refillPerSec() {
        return refillPerSec;
    }

    public void setRefillPerSec(double refillPerSec) {
        this.refillPerSec = refillPerSec;
    }
}
