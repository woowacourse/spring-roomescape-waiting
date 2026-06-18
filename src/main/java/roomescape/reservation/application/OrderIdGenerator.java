package roomescape.reservation.application;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OrderIdGenerator {

    public String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public String generateIdempotencyKey() {
        return UUID.randomUUID().toString();
    }
}
