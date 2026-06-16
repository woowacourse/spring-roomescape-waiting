package roomescape.payment.toss;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.payment.PaymentConfirmation;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_REAL_API", matches = "true")
@EnabledIfEnvironmentVariable(named = "TOSS_SECRET_KEY", matches = ".+")
class TossPaymentRealApiTest {

    @Autowired
    private TossPaymentGateway tossPaymentGateway;

    @DynamicPropertySource
    static void tossProperties(DynamicPropertyRegistry registry) {
        registry.add("toss.base-url", () -> "https://api.tosspayments.com");
        registry.add("toss.secret-key", () -> System.getenv("TOSS_SECRET_KEY"));
    }

    @Test
    void nonExistingPaymentKeyMapsToDomainExceptionTest() {
        assertThatThrownBy(() -> tossPaymentGateway.confirm(
                new PaymentConfirmation("tgen_does_not_exist_payment_key", "real-api-test-order-id", 1000L)))
                .isInstanceOf(RoomEscapeException.class);
    }
}
