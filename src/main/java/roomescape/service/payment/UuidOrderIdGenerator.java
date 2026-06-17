package roomescape.service.payment;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UuidOrderIdGenerator implements OrderIdGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
