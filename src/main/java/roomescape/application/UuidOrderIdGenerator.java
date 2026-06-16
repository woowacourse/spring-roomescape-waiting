package roomescape.application;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UuidOrderIdGenerator implements OrderIdGenerator {

    @Override
    public String generate() {
        // "order_" + 32 hex = 38자, 영숫자/_ → 요구사항(6~64자) 충족
        return "order_" + UUID.randomUUID().toString().replace("-", "");
    }
}
