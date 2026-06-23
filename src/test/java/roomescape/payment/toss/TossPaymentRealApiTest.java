package roomescape.payment.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentStatus;
import roomescape.common.exception.BusinessException;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIfEnvironmentVariable(named = "RUN_REAL_API", matches = "true")
class TossPaymentRealApiTest {

    @Autowired
    private TossPaymentGateway tossPaymentGateway;

    @DynamicPropertySource
    static void tossProperties(DynamicPropertyRegistry registry) {
        registry.add("payment.toss.base-url", () -> "https://api.tosspayments.com");
        registry.add("payment.toss.secret-key", () -> requiredEnvironmentVariable("TOSS_SECRET_KEY"));
    }

    @Test
    void 존재하지_않는_결제도_Toss_DTO가_아닌_도메인_예외로_변환한다() {
        PaymentConfirmation confirmation = new PaymentConfirmation(
                "tgen_does_not_exist_payment_key",
                "real-api-test-order-id",
                1_000L
        );

        assertThatThrownBy(() -> tossPaymentGateway.confirm(confirmation))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 브라우저에서_인증한_샌드박스_결제를_승인하면_DONE을_반환한다() {
        String paymentKey = System.getenv("TOSS_TEST_PAYMENT_KEY");
        String orderId = System.getenv("TOSS_TEST_ORDER_ID");
        String rawAmount = System.getenv("TOSS_TEST_AMOUNT");
        assumeTrue(paymentKey != null && orderId != null && rawAmount != null,
                "브라우저 인증 결과 환경변수가 있을 때만 성공 승인 테스트를 실행합니다.");

        long amount = Long.parseLong(rawAmount);
        var result = tossPaymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(result.approvedAmount()).isEqualTo(amount);
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " 환경변수가 필요합니다.");
        }
        return value;
    }
}
