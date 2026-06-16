package roomescape.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;

@TestConfiguration
public class TestPaymentGatewayConfig {

    @Bean
    @Primary
    public PaymentGateway testPaymentGateway() {
        return this::success;
    }

    private PaymentResult success(PaymentConfirmation confirmation) {
        return new PaymentResult(confirmation.paymentKey(), confirmation.orderId(), "DONE",
                confirmation.amount());
    }
}
