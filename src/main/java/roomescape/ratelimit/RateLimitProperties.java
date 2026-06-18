package roomescape.ratelimit;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(
        boolean enabled,
        long capacity,
        double refillPerSecond,
        List<String> pathPatterns
) {

    public RateLimitProperties {
        if (capacity == 0) {
            capacity = 60;
        }
        if (refillPerSecond == 0D) {
            refillPerSecond = 1D;
        }
        pathPatterns = pathPatterns == null || pathPatterns.isEmpty()
                ? List.of("/reservations", "/reservations/**", "/payments", "/payments/**")
                : List.copyOf(pathPatterns);
    }
}
