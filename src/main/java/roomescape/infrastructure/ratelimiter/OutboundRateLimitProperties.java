package roomescape.infrastructure.ratelimiter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "outbound-rate-limit")
public class OutboundRateLimitProperties {
    private double capacity = 30.0;
    private double refillPerSec = 10.0;

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
