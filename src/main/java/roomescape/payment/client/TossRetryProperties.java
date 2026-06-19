package roomescape.payment.client;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "toss.retry")
public record TossRetryProperties(
        @DefaultValue("3") int maxAttempts,
        @DefaultValue("PT1S") Duration fallbackWait
) {

    public TossRetryProperties {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("toss.retry.max-attempts must be at least 1");
        }
    }
}