package roomescape.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "outbound-rate-limit")
public class OutboundRateLimitProperties {
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
