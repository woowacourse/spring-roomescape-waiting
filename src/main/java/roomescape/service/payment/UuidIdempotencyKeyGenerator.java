package roomescape.service.payment;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UuidIdempotencyKeyGenerator implements IdempotencyKeyGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
