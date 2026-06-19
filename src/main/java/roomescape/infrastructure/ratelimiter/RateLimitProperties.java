package roomescape.infrastructure.ratelimiter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {
    private double capacity = 10.0;
    private double refillPerSec = 2.0;

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public double getRefillPerSec() {
        return refillPerSec;
    }

    public void setRefillPerSec(double refillPerSec) {
        this.refillPerSec = refillPerSec;
    }
}
