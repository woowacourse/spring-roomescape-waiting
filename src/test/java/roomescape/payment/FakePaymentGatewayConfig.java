package roomescape.payment;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class FakePaymentGatewayConfig {

    @Bean
    @Primary
    public FakePaymentGateway fakePaymentGateway() {
        return new FakePaymentGateway();
    }
}
