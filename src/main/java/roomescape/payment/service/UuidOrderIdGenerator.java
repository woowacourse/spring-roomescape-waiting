package roomescape.payment.service;

import org.springframework.stereotype.Component;
import roomescape.payment.domain.OrderIdGenerator;

import java.util.UUID;

@Component
public class UuidOrderIdGenerator implements OrderIdGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
