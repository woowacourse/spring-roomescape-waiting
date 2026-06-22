package roomescape.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "outbound-rate-limit")
public class OutboundRateLimitProperties {

    private double capacity;
    private double refillPerSec;

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